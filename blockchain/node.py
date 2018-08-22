from datetime import datetime
import aiohttp
import asyncio
from sanic import Sanic
from sanic.response import json, text, redirect, html
from tasks import initiate_node, peer_check, peer_discovery, consensus
from helpers import add_peer, get_peers, get_config, get_products, add_product, get_hostname, sell_product
from argparse import ArgumentParser
from mining import mining_once
# from sanic_wtf import SanicForm
# from wtforms import SubmitField, TextField
# from wtforms.validators import DataRequired, Length

app = Sanic()
initiate_node(app)
app.add_task(peer_check(app))
app.add_task(peer_discovery(app))
app.add_task(consensus(app))

# app.config['SECRET_KEY'] = 'top secret !!!'
# session = {}


# class ProductForm(SanicForm):
#     product = TextField('Product', validators=[DataRequired(), Length(max=40)])
#     submit = SubmitField('Submit')


@app.route('/')
async def check_alive(request):
    return text('I am alive!', status=200)


@app.route('/consensus', methods=['GET'])
async def consensus(request):
    app.mining = False
    replaced = await app.blockchain.as_resolve_conflicts()
    if replaced:
        response = {
            'message': 'Our chain was replaced'
        }
    else:
        response = {
            'message': 'Our chain is authoritative'
        }
    app.mining = True
    return json(response, status=200)


@app.route('/id', methods=['GET'])
async def get_id(request):
    return json({'id': get_config(key='node_identifier')}, status=200)
    # id = get_config(key='node_identifier')
    # content = f"""
    # <h1>My ID</h1><br>
    # <label>My User Id : </label>{id}
    #         """
    # return html(content)


@app.route('/mine/start', methods=['GET'])
async def start_mine(request):
    app.mining = True
    return text(body='mining start', status=200)


@app.route('/mine/stop', methods=['GET'])
async def stop_mine(request):
    app.mining = False
    return text(body='mining stop', status=200)


@app.route('/nodes/register', methods=['POST'])
async def register_nodes(request):
    values = request.get_json()
    nodes = values.get('nodes')
    if nodes is None:
        return text(body="Error: Please supply a valid list of nodes", status=400)
    for node in nodes:
        add_peer(node['identifier'], node['hostname'])
    peers = get_peers()
    response = {
        'message': 'New nodes have been added',
        'total_nodes': [peer.to_dict() for peer in peers]
    }
    return json(response, status=201)


@app.route('/products', methods=['GET'])
async def products(request):
    my_products = get_products()
    if my_products:
        return json(list(my_products), status=200)
    return json(list(), status=200)


@app.route('/search', methods=['GET'])
async def search_products(request):
    for peer in get_peers():
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(f'{peer.hostname}/products') as response:
                    if response.status == 200:
                        new_product_list = {}
                        for product in await response.json():
                            new_product_list[product['product_id']] = product['description']
                        app.product_list[peer.identifier] = new_product_list
        except (aiohttp.client_exceptions.ClientConnectorError, asyncio.TimeoutError):
            continue
    return json(app.product_list, status=200)


@app.route('/products/add', methods=['GET', 'POST'])
async def add_products(request):
    # form = ProductForm(request)
    # if request.method == 'POST' and form.validate():
    #     product = form.product.data
    #     msg = '{} - {}'.format(datetime.now(), product)
    #     session.setdefault('product', []).append(msg)
    #     add_product(description=product, creator_id=get_config(key='node_identifier'))
    #     # response = {
    #     #     'message': 'products added',
    #     #     'total_products': list(get_products())
    #     # }
    #     # return json(response, status=201)
    #
    #     return redirect('/products/add')
    #
    # content = """
    #     <h1>Add Products</h1>
    #     <table style="width:100%">
    #     <tr>
    #         <th>product_id</th>
    #         <th>creator_id</th>
    #         <th>status</th>
    #         <th>description</th>
    #     </tr>"""
    # for product in list(get_products()):
    #     content += f"""
    #             <tr>
    #                 <td>{product['product_id']}</td>
    #                 <td>{product['creator_id']}</td>
    #                 <td>{product['status']}</td>
    #                 <td>{product['description']}</td>
    #             </tr>
    #             """
    # content += f"""
    # </table>
    #     <form action="" method="POST">
    #       {'<br>'.join(form.product.errors)}
    #       <br>
    #       {form.product(size=40, placeholder="input product description here")}
    #       {form.submit}
    #     </form>
    #     """
    # return html(content)

    # archived json ret
    values = request.json
    descriptions = values.get('products')
    if descriptions is None:
        return text(body="Error: invalid product descriptions", status=400)
    for description in descriptions:
        add_product(description=description)
    response = {
        'message': 'products added',
        'total_products': list(get_products())
    }
    return json(response, status=201)


@app.route('/buy', methods=['POST'])
async def buy(request):
    values = request.json
    description = values.get('description')
    if description is None:
        return text(body='Missing values', status=400)
    buyer = get_config(key='node_identifier')
    for seller, product_dict in app.product_list.items():
        for product_id, product_description in product_dict.items():
            if product_description == description:
                hostname = get_hostname(seller)
                total_response = {'buyer_message': f'{buyer} wants to buy {seller}\'s product {product_description}'}
                try:
                    async with aiohttp.ClientSession() as session:
                        async with session.post(f'{hostname}/sell', json={
                            'product_id': product_id,
                            'recipient': buyer
                        }) as response:
                            if response.status == 200:
                                total_response['seller_message'] = await response.text()
                                add_product(product_id=product_id, description=description)
                            else:
                                return text(body=await response.text(), status=404)
                except (aiohttp.client_exceptions.ClientConnectorError, asyncio.TimeoutError):
                    return text(body='Server not found', status=404)
                return json(total_response, status=200)


@app.route('/sell', methods=['POST'])
async def sell(request):
    values = request.json
    required = ['product_id', 'recipient']
    if not all(k in values for k in required):
        return text(body='Missing values', status=400)
    seller = get_config(key='node_identifier')
    product_id = values.get('product_id')
    buyer = values.get('recipient')
    if sell_product(product_id):
        app.blockchain.new_transaction(seller, buyer, product_id)
        transcation = await mining_once(app)
        product = get_products(product_id=product_id)
        return text(f'Sold {product.description} to {buyer}', 200)
    return text('Sold out', 404)


@app.route('/transactions')
async def current_transactions(request):
    if request.method == 'GET':
        return json(app.blockchain.current_transactions, status=200)
    elif request.method == 'POST':
        values = request.json
        required = ['sender', 'recipient', 'product_id']
        if not all(k in values for k in required):
            return text(body='Missing values', status=400)
        index = app.blockchain.new_transaction(values['sender'], values['recipient'], values['product_id'])
        response = {'message': f'Transaction will be added to Block {index}'}
        return json(response, status=201)


@app.route('/transactions/trace', methods=['POST'])
async def trace_transaction(request):
    values = request.json
    required = ['recipient', 'description']
    if not all(k in values for k in required):
        return text(body='Missing values', status=400)
    my_product = get_products(description=values['description'])
    if my_product:
        product_id = my_product.product_id
        senders = app.blockchain.trace_transaction(values['recipient'], product_id)
        response = {
            'senders': senders,
            'length': len(senders)
        }
        return json(response, status=200)
    return text('You don\'t have this product', status=404)


@app.route('/blocks', methods=['GET'])
async def blocks(request):
    all_blocks = app.blockchain.get_blocks()
    for block in all_blocks:
        block['timestamp'] = str(block['timestamp'])
    return json(all_blocks, status=200)


if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument('-p', '--port', default=5000, type=int, help='port to listen on')
    args = parser.parse_args()
    port = args.port
    # only use for no public network device
    # PortMapper().add_portmapping(port, port, 'TCP')
    app.run(host='0.0.0.0', port=port, debug=True, access_log=True)
