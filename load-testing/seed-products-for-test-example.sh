#!/bin/bash

# Rename to seed-products-for-test.sh and fill in the token from keycloak

TOKEN="<YOUR_KEYCLOAK_TOKEN>"
BASE_URL="http://localhost:8080/api/v1/products"

declare -a products=(
  '{"name": "Wireless Headphones", "description": "Premium noise-cancelling wireless headphones with 30-hour battery life", "price": 149.99, "category": "ELECTRONICS", "initialQuantity": 1000}'
  '{"name": "Mechanical Keyboard", "description": "RGB backlit mechanical keyboard with hot-swappable switches", "price": 89.99, "category": "ELECTRONICS", "initialQuantity": 1000}'
  '{"name": "USB-C Hub", "description": "7-in-1 USB-C hub with HDMI, ethernet and card reader", "price": 39.99, "category": "ELECTRONICS", "initialQuantity": 1000}'
  '{"name": "Running Shoes", "description": "Lightweight running shoes with breathable mesh upper", "price": 79.99, "category": "SPORTS", "initialQuantity": 1000}'
  '{"name": "Yoga Mat", "description": "Non-slip eco-friendly yoga mat, 6mm thick", "price": 24.99, "category": "SPORTS", "initialQuantity": 1000}'
  '{"name": "Coffee Grinder", "description": "Electric burr coffee grinder with 18 grind settings", "price": 59.99, "category": "HOME_AND_GARDEN", "initialQuantity": 1000}'
  '{"name": "Desk Lamp", "description": "LED desk lamp with adjustable brightness and color temperature", "price": 34.99, "category": "HOME_AND_GARDEN", "initialQuantity": 1000}'
  '{"name": "Graphic Novel Set", "description": "Collector edition graphic novel box set, 3 volumes", "price": 54.99, "category": "BOOKS", "initialQuantity": 1000}'
  '{"name": "Protein Shaker Bottle", "description": "Insulated stainless steel shaker bottle, 1L capacity", "price": 19.99, "category": "HEALTH_AND_BEAUTY", "initialQuantity": 1000}'
  '{"name": "Bluetooth Speaker", "description": "Portable waterproof bluetooth speaker with 12-hour playtime", "price": 44.99, "category": "ELECTRONICS", "initialQuantity": 1000}'
)

for product in "${products[@]}"; do
  echo "Creating: $product"
  curl --location "$BASE_URL" \
    --header 'Content-Type: application/json' \
    --header "Authorization: Bearer $TOKEN" \
    --data "$product"
  echo -e "\n---"
done