<?php

/**
 * Azam TV Channel Aggregator
 * Compatible na PHP 7.2+ na InfinityFree hosting
 * Sources: BailaTV, ZimoTV, PixTVMax
 */

// ============================================================
// HEADERS
// ============================================================

$isDownload = isset($_GET['download']) && $_GET['download'] === '1';

header('Access-Control-Allow-Origin: *');
header('Content-Type: ' . ($isDownload ? 'application/octet-stream' : 'application/json'));

if ($isDownload) {
    header('Content-Disposition: attachment; filename=channels.json');
}

// ============================================================
// CONFIG
// ============================================================

define('CURL_TOUT', 15);
define('MY_UA',  'Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/112.0 Mobile Safari/537.36');
define('MY_REF', 'https://bailatv.live/');

$LIPOPO_CHANNELS = [
    ['url' => 'https://bailatv.live/one.php',   'name' => 'Azam One',      'category' => 'TAMTHILIYA', 'image' => 'https://i.postimg.cc/RFfMP31f/1770047388328-Master-Chef-Azam-ONE-poster-Image.webp'],
    ['url' => 'https://bailatv.live/kix.php',   'name' => 'Kix TV',        'category' => 'MUSIC',      'image' => 'https://i.postimg.cc/pTYdyxDW/1745514813150-Crown-TVPoster-Image.webp'],
    ['url' => 'https://bailatv.live/cheka.php', 'name' => 'Cheka Plus TV', 'category' => 'MUSIC',      'image' => 'https://i.postimg.cc/T2Fqj5jf/1746270439707-Cheka-Plus-TV-poster-Image.webp'],
    ['url' => 'https://bailatv.live/zama.php',  'name' => 'Zamaradi TV',   'category' => 'MUSIC',      'image' => 'https://i.postimg.cc/0rgLy7wK/Zamaradi-TV-d7c13bcf55a3290fd85d8155f0888e85.png'],
];

$ZIMO_KEY_MAP = [
    'sports 1' => ['name' => 'AzamSports 1 HD', 'key' => 'c31df1600afc33799ecac543331803f2:dd2101530e222f545997d4c553787f85', 'category' => 'NBC PREMIER LEAGUE'],
    'sports 2' => ['name' => 'AzamSports 2 HD', 'key' => '739e7499125b31cc9948da8057b84cf9:1b7d44d798c351acc02f33ddfbb7682a', 'category' => 'NBC PREMIER LEAGUE'],
    'sports 3' => ['name' => 'AzamSports 3 HD', 'key' => '2f12d7b889de381a9fb5326ca3aa166d:51c2d733a54306fdf89acd4c9d4f6005', 'category' => 'NBC PREMIER LEAGUE'],
    'sports 4' => ['name' => 'AzamSports 4 HD', 'key' => '1606cddebd3c36308ec5072350fb790a:04ece212a9201531afdd91c6f468e0b3', 'category' => 'NBC PREMIER LEAGUE'],
    'azm two'  => ['name' => 'Azam Two',        'key' => '3b92b644635f3bad9f7d09ded676ec47:d012a9d5834f69be1313d4864d150a5f', 'category' => 'TAMTHILIYA'],
    'sinema'   => ['name' => 'Sinema Zetu',     'key' => 'd628ae37a8f0336b970f250d9699461e:1194c3d60bb494aabe9114ca46c2738e', 'category' => 'TAMTHILIYA'],
    'utv'      => ['name' => 'UTV',             'key' => '31b8fc6289fe3ca698588a59d845160c:f8c4e73f419cb80db3bdf4a974e31894', 'category' => 'OTHER CHANNELS'],
    'wasafi'   => ['name' => 'Wasafi TV',       'key' => '8714fe102679348e9c76cfd315dacaa0:a8b86ceda831061c13c7c4c67bd77f8e', 'category' => 'MUSIC'],
    'zbc'      => ['name' => 'ZBC',             'key' => '2d60429f7d043a638beb7349ae25f008:f9b38900f31ce549425df1de2ea28f9d', 'category' => 'OTHER CHANNELS'],
];

$ZIMO_CATEGORIES = [
    'local channels', 'international', 'sports',
    'movies', 'music', 'kids', 'news', 'religious',
];

// PixTVMax category IDs
$PIXTV_CATEGORIES = [
    '1769178540796',
    // Ongeza IDs zaidi hapa kama unapata
];

// ============================================================
// HELPERS
// ============================================================

function my_str_contains($haystack, $needle) {
    return strpos($haystack, $needle) !== false;
}

function my_str_ends_with($haystack, $needle) {
    return substr($haystack, -strlen($needle)) === $needle;
}

function fetchUrl($url) {
    $context = stream_context_create([
        'http' => [
            'method'     => 'GET',
            'timeout'    => CURL_TOUT,
            'user_agent' => MY_UA,
            'header'     => "Referer: " . MY_REF . "\r\nAccept: text/html,application/json,*/*\r\n",
        ],
        'ssl' => [
            'verify_peer'      => false,
            'verify_peer_name' => false,
        ],
    ]);

    $result = @file_get_contents($url, false, $context);

    if ($result === false && function_exists('curl_init')) {
        $ch = curl_init();
        curl_setopt_array($ch, [
            CURLOPT_URL            => $url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_TIMEOUT        => CURL_TOUT,
            CURLOPT_SSL_VERIFYPEER => false,
            CURLOPT_SSL_VERIFYHOST => false,
            CURLOPT_HTTPHEADER     => [
                'User-Agent: ' . MY_UA,
                'Referer: '    . MY_REF,
                'Accept: text/html,application/json,*/*',
            ],
        ]);
        $result = curl_exec($ch);
        curl_close($ch);
    }

    return is_string($result) ? $result : '';
}

function extractStreamData($html) {
    $streamUrl = '';
    $clearKey  = '';
    if (preg_match('/var\s+streamUrl\s*=\s*["\']([^"\']+)["\']/', $html, $m)) {
        $streamUrl = trim($m[1]);
    }
    if (preg_match('/var\s+clearKey\s*=\s*["\']([^"\']+)["\']/', $html, $m)) {
        $clearKey = trim($m[1]);
    }
    return ['stream_url' => $streamUrl, 'clear_key' => $clearKey];
}

function streamType($url) {
    return my_str_ends_with($url, '.m3u8') ? 'hls' : 'dash';
}

function buildChannel($name, $category, $url, $image, $key) {
    return [
        'name'     => $name,
        'category' => $category,
        'url'      => $url,
        'image'    => $image,
        'key'      => $key ? $key : null,
        'type'     => streamType($url),
    ];
}

function guessCategory($title) {
    $t = strtolower($title);
    if (my_str_contains($t, 'sport') || my_str_contains($t, 'premier') || my_str_contains($t, 'liga'))   return 'SPORTS';
    if (my_str_contains($t, 'music') || my_str_contains($t, 'wasafi') || my_str_contains($t, 'cheka'))   return 'MUSIC';
    if (my_str_contains($t, 'news')  || my_str_contains($t, 'habari') || my_str_contains($t, 'tv8'))     return 'NEWS';
    if (my_str_contains($t, 'movie') || my_str_contains($t, 'sinema') || my_str_contains($t, 'film'))    return 'MOVIES';
    if (my_str_contains($t, 'kid')   || my_str_contains($t, 'cartoon') || my_str_contains($t, 'junior')) return 'KIDS';
    if (my_str_contains($t, 'dini')  || my_str_contains($t, 'imani')  || my_str_contains($t, 'religi'))  return 'RELIGIOUS';
    return 'OTHER CHANNELS';
}

// ============================================================
// SEHEMU 1: BAILATV
// ============================================================

$lipopoChannels = [];

foreach ($LIPOPO_CHANNELS as $item) {
    $html = fetchUrl($item['url']);
    $data = extractStreamData($html);
    if (empty($data['stream_url'])) continue;

    $lipopoChannels[] = buildChannel(
        $item['name'],
        $item['category'],
        $data['stream_url'],
        $item['image'],
        $data['clear_key'] ? $data['clear_key'] : null
    );
}

// ============================================================
// SEHEMU 2: ZIMOTV
// ============================================================

$zimoChannels = [];
$seen         = [];

foreach ($ZIMO_CATEGORIES as $cat) {
    $body = fetchUrl('https://zimotv.com/mb/api/get-channels.php?category=' . urlencode($cat));
    $data = json_decode($body, true);
    if (empty($data['channels']) || !is_array($data['channels'])) continue;

    foreach ($data['channels'] as $ch) {
        $chUrl = trim(isset($ch['url'])   ? $ch['url']   : '');
        $title = trim(isset($ch['title']) ? $ch['title'] : '');
        if (empty($chUrl) || empty($title) || isset($seen[$chUrl])) continue;
        $seen[$chUrl] = true;

        $titleLower = strtolower($title);
        $key        = null;
        $name       = $title;
        $category   = guessCategory($title);

        foreach ($ZIMO_KEY_MAP as $keyword => $info) {
            if (my_str_contains($titleLower, $keyword)) {
                $key      = $info['key'];
                $name     = $info['name'];
                $category = $info['category'];
                break;
            }
        }

        // Ruka ZimoTV channels ambazo hazina clearkey
        if (empty($key)) continue;

        $zimoChannels[] = buildChannel($name, $category, $chUrl, isset($ch['logo']) ? $ch['logo'] : null, $key);
    }
}

// ============================================================
// SEHEMU 3: PIXTVMAX
// ============================================================

$pixtvChannels = [];

foreach ($PIXTV_CATEGORIES as $catId) {
    $body = fetchUrl('https://pixtvmax.quest/api/categories/' . $catId . '/channels');
    $channels = json_decode($body, true);
    if (!is_array($channels)) continue;

    foreach ($channels as $ch) {
        // Ruka channels ambazo hazina mpd_url
        $mpd = trim(isset($ch['mpd_url']) ? $ch['mpd_url'] : '');
        if (empty($mpd)) continue;

        $name    = trim(isset($ch['name'])     ? $ch['name']     : '');
        $image   = isset($ch['logo_url'])      ? $ch['logo_url'] : null;
        $drmType = isset($ch['drm_type'])      ? strtoupper($ch['drm_type']) : 'NONE';

        // Ruka channels zenye Widevine/PlayReady (zinahitaji license server)
        if ($drmType === 'WIDEVINE' || $drmType === 'PLAYREADY') continue;

        // Ruka kama tayari ipo kwenye seen
        if (isset($seen[$mpd])) continue;
        $seen[$mpd] = true;

        // Clearkey — inatoka moja kwa moja kwenye API
        $key = null;
        if ($drmType === 'CLEARKEY' && isset($ch['license_url']) && !empty($ch['license_url'])) {
            $key = $ch['license_url'];
        }

        // Kama mpd_url inaelekeza BailaTV, ichuje stream_url halisi
        if (my_str_contains($mpd, 'bailatv.live')) {
            $html = fetchUrl($mpd);
            $data = extractStreamData($html);
            if (empty($data['stream_url'])) continue;
            $mpd = $data['stream_url'];
            if (empty($key) && !empty($data['clear_key'])) {
                $key = $data['clear_key'];
            }
        }

        // Ruka MPD ambazo hazina clearkey (isipokuwa drm_type ni NONE)
        if ($drmType !== 'NONE' && empty($key)) continue;

        $category = guessCategory($name);
        $pixtvChannels[] = buildChannel($name, $category, $mpd, $image, $key);
    }
}

// ============================================================
// UNGANISHA, ONDOA NAKALA, PANGA, NA TUMA
// ============================================================

$allChannels = array_merge($lipopoChannels, $zimoChannels, $pixtvChannels);

// Deduplication kwa jina (case-insensitive)
$finalChannels = [];
$finalSeen     = [];
foreach ($allChannels as $ch) {
    $nameKey = strtolower(trim($ch['name']));
    if (isset($finalSeen[$nameKey])) continue;
    $finalSeen[$nameKey] = true;
    $finalChannels[] = $ch;
}

// Panga kwa category kisha jina
usort($finalChannels, function ($a, $b) {
    $cat = strcmp($a['category'], $b['category']);
    return $cat !== 0 ? $cat : strcmp($a['name'], $b['name']);
});

$json = json_encode([
    'success'    => true,
    'count'      => count($finalChannels),
    'fetched_at' => date('Y-m-d H:i:s'),
    'channels'   => $finalChannels,
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

if ($isDownload) {
    header('Content-Length: ' . strlen($json));
}

echo $json;
