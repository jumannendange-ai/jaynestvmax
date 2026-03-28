FROM php:8.1-apache

# Enable curl extension
RUN docker-php-ext-install curl

# Copy files
COPY azam.php /var/www/html/azam.php

# Apache config
RUN a2enmod rewrite
EXPOSE 8080

CMD ["apache2-foreground"]
