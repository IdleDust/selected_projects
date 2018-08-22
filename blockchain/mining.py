import aiohttp
import requests
import asyncio
import logging
import multiprocessing
from datetime import datetime
from helpers import hash_block, get_config, get_peers

logger = logging.getLogger('root.mining')
default_difficulty = 4


def valid_proof(current_block):
    target = str.ljust("0" * default_difficulty, 64, "f")
    guess_hash = hash_block(current_block)
    return guess_hash <= target


def valid_blocks(blocks):
    last_block = blocks[0]
    current_index = 1
    while current_index < len(blocks):
        block = blocks[current_index]
        if block['previous_hash'] != hash_block(last_block):
            return False
        if not valid_proof(block):
            return False
        last_block = block
        current_index += 1
    return True


def proof_of_work(current_block, difficulty, event):
    target = str.ljust("0" * difficulty, 64, "f")
    guess_hash = hash_block(current_block)
    while guess_hash > target:
        current_block['timestamp'] = datetime.utcnow()
        current_block['proof'] += 1
        guess_hash = hash_block(current_block)
    current_block['hash'] = guess_hash
    return current_block


def miner(pipe, event):
    while True:
        task = pipe.recv()
        logger.debug(f"Received new mining task with difficulty {task['difficulty']}")
        if task:
            found_block = proof_of_work(task['block'], task['difficulty'], event)
            pipe.send({'found_block': found_block})


async def mining_controller(app):
    app.blockchain.resolve_conflicts()
    pipe, remote_pipe = multiprocessing.Pipe()
    event = multiprocessing.Event()
    process = multiprocessing.Process(target=miner, args=(remote_pipe, event))
    process.start()
    pipe.send({'block': app.blockchain.build_block(), 'difficulty': default_difficulty})
    while True:
        event.set()
        await asyncio.sleep(10)
        if not app.mining:
            event.clear()
        if pipe.poll():
            result = pipe.recv()
            found_block = result['found_block']
            if app.blockchain.build_block()['previous_hash'] == found_block['previous_hash']:
                app.blockchain.save_block(found_block)
                app.blockchain.new_transaction(
                    sender="0",
                    recipient=get_config(key='node_identifier'),
                    product_id=1,
                )
                logger.info(
                    f"Mined Block {found_block['height']} containing {len(found_block['transactions'])} transactions")
                await as_publish_consensus()
            pipe.send({'block': app.blockchain.build_block(), 'difficulty': default_difficulty})


async def mining_once(app):
    app.blockchain.resolve_conflicts()
    found_block = proof_of_work(app.blockchain.build_block(), default_difficulty, None)
    if app.blockchain.build_block()['previous_hash'] == found_block['previous_hash']:
        app.blockchain.save_block(found_block)
        app.blockchain.new_transaction(
            sender="0",
            recipient=get_config(key='node_identifier'),
            product_id=1,
        )
        logger.info(
            f"Mined Block {found_block['height']} containing {len(found_block['transactions'])} transactions")
        await as_publish_consensus()
        return found_block['transactions']


async def as_publish_consensus():
    peers = get_peers()
    for peer in peers:
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(f'{peer.hostname}/consensus') as response:
                    logger.debug(f'publish consensus: {peer.hostname} => {response.status}')
        except (aiohttp.client_exceptions.ClientConnectorError, asyncio.TimeoutError):
            continue
    return True


def publish_consensus():
    peers = get_peers()
    for peer in peers:
        try:
            response = requests.get(f'{peer.hostname}/consensus')
            logger.debug(f'publish consensus: {peer.hostname} => {response.status_code}')
        except (requests.ConnectionError, requests.ConnectTimeout):
            continue
