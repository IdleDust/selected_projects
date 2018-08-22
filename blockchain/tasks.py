import asyncio
import requests
import logging
import aiohttp
from database import db, reset_db
from helpers import get_random_peers, add_peer, check_peer, get_peers, get_config
from mining import valid_blocks
from blockchain import Blockchain
import re

logger = logging.getLogger('root.tasks')
basic_range = set(range(5000, 5010, 1))


def initiate_node(app):
    reset_db()
    app.node_identifier = get_config(key='node_identifier')
    logger.info('Node Identifier: %s', app.node_identifier)
    app.blockchain = Blockchain()
    app.product_list = {}
    app.mining = True
    app.peer_initialized = False
    peer_init(app)


async def peer_check(app):
    while True:
        peers = get_random_peers()
        for peer in peers:
            try:
                async with aiohttp.ClientSession() as session:
                    async with session.get(peer.hostname) as response:
                        logger.debug(f'check peer: {peer.hostname} => {response.status}')
            except (
                    aiohttp.client_exceptions.InvalidURL, aiohttp.client_exceptions.ClientConnectorError,
                    asyncio.TimeoutError):
                db.delete(peer)
                db.commit()
                logger.debug(f'{peer.hostname}: Deleted node')
                basic_range.add(re.search('\d{4}$', peer.hostname).group(0))
        await asyncio.sleep(10)


async def peer_discovery(app):
    while True:
        search_range = list(basic_range)
        for port in search_range:
            try:
                async with aiohttp.ClientSession() as session:
                    async with session.get(f'http://0.0.0.0:{port}/id') as response:
                        if response.status == 200:
                            data = await response.json()
                            if app.node_identifier != data['id'] and not check_peer(data['id']):
                                add_peer(data['id'], f'http://0.0.0.0:{port}')
                                logger.info(f'Found new node: http://0.0.0.0:{port}')
                            basic_range.remove(port)
            except (
                    aiohttp.client_exceptions.InvalidURL, aiohttp.client_exceptions.ClientConnectorError,
                    asyncio.TimeoutError):
                continue
        app.peer_initialized = True
        await asyncio.sleep(5)


def peer_init(app):
    search_range = list(basic_range)
    for port in search_range:
        try:
            response = requests.get(f'http://0.0.0.0:{port}/id')
            if response.status_code == 200:
                data = response.json()
                if app.node_identifier != data['id'] and not check_peer(data['id']):
                    add_peer(data['id'], f'http://0.0.0.0:{port}')
                    logger.info(f'Found new node: http://0.0.0.0:{port}')
                basic_range.remove(port)
        except (requests.ConnectionError, requests.ConnectTimeout):
            continue
    return True


async def consensus(app):
    while True:
        new_chain = None
        max_length = app.blockchain.get_length(app.blockchain.get_blocks())
        peers = get_peers()
        for peer in peers:
            try:
                async with aiohttp.ClientSession() as session:
                    async with session.get(f'{peer.hostname}/blocks') as response:
                        if response.status == 200:
                            blocks = await response.json()
                            length = app.blockchain.get_length(blocks)
                            if length > max_length and valid_blocks(blocks):
                                max_length = length
                                new_chain = blocks
            except (
                    aiohttp.client_exceptions.InvalidURL, aiohttp.client_exceptions.ClientConnectorError,
                    asyncio.TimeoutError):
                continue
        if new_chain:
            app.blockchain.replace_blocks(blocks)
        await asyncio.sleep(4)


async def watch_blockchain(app):
    while True:
        print(f'TXN: {app.blockchain.current_transactions}')
        await asyncio.sleep(2)
