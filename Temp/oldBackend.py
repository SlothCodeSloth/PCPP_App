from flask import Flask, request, jsonify
from pypartpicker import Scraper

app = Flask(__name__)
pcpp = Client()

@app.route('/search', methods=['GET'])
def search_parts():
    query = request.args.get('query')
    limit = int(request.args.get('limit', 20))
    region = request.args.get('region', 'us')

    try:
        result = pcpp.get_part_search(query, region=region)
        parts = result.parts[:limit]


        results = [{
            'name':part.name,
            'url':part.url,
            'price':part.cheapest_price.total if part.cheapest_price else None,
            'image': part.image_urls[0] if part.image_urls else None}
            for part in parts]
        return jsonify(results)
    except Exception as e:
        return jsonify({'error':str(e)}), 500

@app.route('/product', methods=['GET'])
def fetch_product():
    url=request.args.get('url')

    try:
        product = pcpp.get_part(url)
        result = {
            'name' : product.name,
            'specs' : product.specs,
            'price_list' : [{
                'seller': vendor.name,
                'value': vendor.price.total,
                'in_stock': vendor.in_stock
            } for vendor in product.vendors] if product.vendors else [],
            'image': product.image_urls[0] if product.image_urls else None,
            'rating': {
                'average': product.rating.average if product.rating else None,
                'count': product.rating.count if product.rating else None
            }
        }
        return jsonify(result)
    except Exception as e:
        return jsonify({'error':str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)

""" 
    python Backend.py
    In cmd, 
    ngrok http 5000
"""