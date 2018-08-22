import aiohttp
import requests
import logging
from datetime import datetime
from database import Block, db
from helpers import hash_block, get_peers, get_products
from mining import valid_blocks

logger = logging.getLogger('root.blockchain')


class Blockchain:
    def __init__(self):
        self.current_transactions = []
        if not self.last_block:
            block = self.build_block()
            block['hash'] = hash_block(block)
            self.save_block(block)
            logger.info("✨ Created genesis block")
        logger.info("Blockchain Initiated")

    @staticmethod
    def get_blocks(height=0):
        blocks = db.query(Block).filter(Block.height >= height).all()
        return [block.to_dict() for block in blocks]

    def build_block(self):
        last_block = self.last_block
        return {
            'height': last_block.height + 1 if last_block else 0,
            'timestamp': datetime.utcnow(),
            'transactions': self.current_transactions,
            'previous_hash': last_block.hash if last_block else 0,
            'proof': -1
        }

    def save_block(self, block_dict):
        logger.debug(block_dict)
        block = Block(**block_dict)
        db.add(block)
        db.commit()
        self.current_transactions = []

    @staticmethod
    def replace_blocks(new_blocks):
        db.query(Block).delete()
        db.commit()
        for block_dict in new_blocks:
            block_dict['timestamp'] = datetime.strptime(block_dict['timestamp'], '%Y-%m-%d %H:%M:%S.%f')
            db.add(Block(**block_dict))
        db.commit()

    def new_transaction(self, sender, recipient, product_id):
        self.current_transactions.append({
            'sender': sender,
            'recipient': recipient,
            'product_id': product_id
        })
        return self.last_block.height + 1

    @staticmethod
    def trace_transaction(recipient, product_id):
        blocks = db.query(Block).all()
        current_index = len(blocks) - 1
        print(current_index)
        senders = set()
        while current_index >= 0:
            block = blocks[current_index]
            for transaction in block.transactions:
                if transaction['recipient'] == recipient \
                        and transaction['product_id'] == product_id \
                        and transaction['sender'] != '0':
                    senders.add(transaction['sender'])
            current_index -= 1
        return list(senders)

    @staticmethod
    def get_length(blocks):
        current_index = len(blocks) - 1
        count = 0
        while current_index >= 0:
            block = blocks[current_index]
            count += len(block['transactions']) + 1
            current_index -= 1
        return count

    @staticmethod
    async def as_resolve_conflicts():
        new_chain = None
        max_length = Blockchain.get_length(Blockchain.get_blocks())
        peers = get_peers()
        for peer in peers:
            try:
                async with aiohttp.ClientSession() as session:
                    async with session.get(f'{peer.hostname}/blocks') as response:
                        if response.status == 200:
                            blocks = await response.json()
                            length = Blockchain.get_length(blocks)
                            if length > max_length and valid_blocks(blocks):
                                max_length = length
                                new_chain = blocks
            except aiohttp.client_exceptions.ClientConnectorError:
                continue
        if new_chain:
            Blockchain.replace_blocks(blocks)
            return True
        return False

    @staticmethod
    def resolve_conflicts():
        new_chain = None
        max_length = Blockchain.get_length(Blockchain.get_blocks())
        peers = get_peers()
        for peer in peers:
            try:
                response = requests.get(f'{peer.hostname}/blocks')
                if response.status_code == 200:
                    blocks = response.json()
                    length = Blockchain.get_length(blocks)
                    if length > max_length and valid_blocks(blocks):
                        max_length = length
                        new_chain = blocks
            except (requests.ConnectionError, requests.ConnectTimeout):
                continue
        if new_chain:
            Blockchain.replace_blocks(blocks)
            return True
        return False

    @property
    def last_block(self):
        return db.query(Block).order_by(Block.height.desc()).first()
