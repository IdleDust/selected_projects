from datetime import datetime
from hashlib import sha256
from sqlalchemy import func, or_
from database import Product, Config, Peer, db
from uuid import uuid4


def set_config(key, value, replace=False):
    config_value = get_config(key)
    if config_value is None:
        db.add(Config(key=key, value=value))
        db.commit()
        return value
    if config_value != value and replace is True:
        db.add(Config(key=key, value=value))
        db.commit()
        return value
    return config_value


def get_config(key, default=None):
    config = db.query(Config).filter_by(key=key).first()
    if config:
        return config.value
    return default


def get_random_peers(limit=10):
    return db.query(Peer).order_by(func.random()).limit(limit)


def get_hostname(identifier):
    peer = db.query(Peer).filter_by(identifier=identifier).first()
    return peer.hostname


def get_peers():
    return db.query(Peer).all()


def check_peer(identifier='0', hostname='http://0.0.0.0:0000'):
    peer = db.query(Peer).filter(or_(Peer.identifier == identifier, Peer.hostname == hostname)).first()
    if peer:
        return True
    return False


def add_peer(identifier, hostname):
    db.add(Peer(identifier=identifier, hostname=hostname, timestamp=datetime.utcnow()))
    db.commit()


def add_product(description, product_id=None, status='in_stock'):
    if not product_id:
        product_id = uuid4().hex
    db.add(Product(product_id=product_id, description=description, creator_id=get_config(key='node_identifier'),
                   status=status))
    db.commit()


def sell_product(product_id):
    product = db.query(Product).filter_by(product_id=product_id).first()
    if product.status == 'in_stock':
        product.status = 'sold_out'
        db.commit()
        return True
    return False


def get_products(product_id='', description=''):
    if description == '' and product_id == '':
        products = db.query(Product).all()
        return [product.to_dict() for product in products]
    else:
        return db.query(Product).filter(
            or_(Product.product_id == product_id, Product.description == description)).first()


def get_creator_id(product_id, default=None):
    product = db.query(Product).filter_by(product_id=product_id).first()
    if product:
        return product.creator_id
    return default


def remove_product(product_id):
    db.query(Product).filter_by(product_id=product_id).delete()
    db.commit()


def hash_block(block):
    byte_array = f"{block['height']}" \
                 f"{block['timestamp']}" \
                 f"{block['transactions']}" \
                 f"{block['previous_hash']}" \
                 f"{block['proof']}".encode()
    return sha256(byte_array).hexdigest()
