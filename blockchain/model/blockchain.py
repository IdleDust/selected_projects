import json, requests
from hashlib import sha256
from time import time
from urllib.parse import urlparse


class Blockchain:
    def __init__(self):
        self.current_transactions = []
        self.chain = []
        self.nodes = set()
        self.new_block(previous_hash='1', proof=100)

    def register_node(self, address):
        parsed_url = urlparse(address)
        if parsed_url.netloc:
            self.nodes.add(parsed_url.netloc)
        elif parsed_url.path:
            self.nodes.add(parsed_url.path)
        else:
            raise ValueError('Invalid URL')

    def valid_chain(self, chain):
        last_block = chain[0]
        current_index = 1
        while current_index < len(chain):
            block = chain[current_index]
            print(f'{last_block}')
            print(f'{block}')
            print("\n-----------\n")
            if block['previous_hash'] != self.hash_block(last_block):
                return False
            if not self.valid_proof(last_block['proof'], block['proof'], block['previous_hash']):
                return False
            last_block = block
            current_index += 1
        return True

    def resolve_conflicts(self):
        neighbours = self.nodes
        new_chain = None
        max_length = self.get_length(self.chain)
        for node in neighbours:
            response = requests.get(f'http://{node}/chain')
            if response.status_code == 200:
                # length = response.json()['length']
                chain = response.json()['chain']
                length = self.get_length(chain)
                if length > max_length and self.valid_chain(chain):
                    max_length = length
                    new_chain = chain
        if new_chain:
            self.chain = new_chain
            return True
        return False

    def new_block(self, proof, previous_hash):
        block = {
            'index': len(self.chain) + 1,
            'timestamp': time(),
            'transactions': self.current_transactions,
            'proof': proof,
            'previous_hash': previous_hash or self.hash_block(self.chain[-1]),
        }
        self.current_transactions = []
        self.chain.append(block)
        return block

    def new_transaction(self, sender, recipient, amount):
        self.current_transactions.append({
            'sender': sender,
            'recipient': recipient,
            'amount': amount,
        })
        return self.last_block['index'] + 1

    def trace_transaction(self, recipient, amount):
        current_index = len(self.chain) - 1
        senders = set()
        while current_index >= 0:
            block = self.chain[current_index]
            for transaction in block['transactions']:
                if transaction['recipient'] == recipient \
                        and transaction['amount'] == amount \
                        and transaction['sender'] != '0':
                    senders.add(transaction['sender'])
            current_index -= 1
        return list(senders)

    @staticmethod
    def get_length(chain):
        current_index = len(chain) - 1
        count = 0
        while current_index >= 0:
            block = chain[current_index]
            count += len(block['transactions']) + 1
            current_index -= 1
        return count

    @property
    def last_block(self):
        return self.chain[-1]

    def proof_of_work(self, last_block):
        last_proof = last_block['proof']
        last_hash = self.hash_block(last_block)
        proof = 0
        while self.valid_proof(last_proof, proof, last_hash) is False:
            proof += 1
        return proof

    @staticmethod
    def hash_block(block):
        byte_array = f"{block['index']}" \
                     f"{block['timestamp']}" \
                     f"{block['transactions']}" \
                     f"{block['previous_hash']}" \
                     f"{block['proof']}".encode()
        return sha256(byte_array).hexdigest()

    @staticmethod
    def valid_proof(last_proof, proof, last_hash):
        guess = f'{last_proof}{proof}{last_hash}'.encode()
        guess_hash = sha256(guess).hexdigest()
        return guess_hash[:4] == "0000"
