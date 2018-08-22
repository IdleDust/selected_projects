import requests
from time import sleep
from pprint import pprint

class Node:
    def __init__(self, url='http://0.0.0.0:5000', name=''):
        self.url = url
        self.name = name.lower()
        resp = requests.get(self.url + '/id')
        self.id = resp.json()['id']

    def __call__(self, *args, **kwargs):
        resp = requests.get(self.url + '/')
        return self.name + ' GET / ' + str(resp.status_code) + '\n' + resp.text

    def get_id(self):
        resp = requests.get(self.url + '/id')
        return self.name + ' GET /id ' + str(resp.status_code) + '\n' + resp.text

    def add_products(self, products):
        resp = requests.post(self.url + '/products/add',
                             json={'products': products})
        return self.name + ' POST /products/add ' + str(resp.status_code) + '\n'

    def search_products(self):
        resp = requests.get(self.url + '/search')
        pprint(resp.json())
        return self.name + ' GET /search ' + str(resp.status_code) + '\n'

    def buy(self, description):
        resp = requests.post(self.url + '/buy',
                             json={'description': description})  # duplicate checking
        pprint(resp.json())
        return self.name + ' POST /buy ' + str(resp.status_code) + '\n'

    def trace_transaction(self, recipient, description):
        resp = requests.post(self.url + '/transactions/trace',
                             json={'recipient': recipient, 'description': description})
        pprint(resp.json())
        return self.name + ' POST /transactions/trace ' + str(resp.status_code) + '\n'

    def blocks(self):
        resp = requests.get(self.url + '/blocks')
        pprint(resp.json())
        return self.name + ' GET /blocks ' + str(resp.status_code) + '\n'

    def __str__(self):
        return self.name


def test():
    node1 = Node('http://0.0.0.0:5000', 'customer')
    node2 = Node('http://0.0.0.0:5001', 'merchant')
    print(node1.get_id())
    print(node2.get_id())
    print(node2.add_products(['Similac Pro-Advance Infant Formula', 'Similac Pro-Sensitive Infant Formula',
                              'Pure Bliss by Similac Infant Formula', 'Similac Advance Infant Formula, Non-GMO',
                              'Go & Grow by Similac Non-GMO Toddler Drink']))
    sleep(1)
    print(node1.search_products())
    print(node1.buy('Similac Pro-Advance Infant Formula'))
    print(node1.search_products())
    sleep(1)
    print(node1.trace_transaction(node1.id, 'Similac Pro-Advance Infant Formula'))
    print(node1.buy('Similac Pro-Sensitive Infant Formula'))
    print(node1.trace_transaction(node1.id, 'Similac Pro-Advance Infant Formula'))
    print(node1.blocks())
    print(node2.blocks())
if __name__ == "__main__":
    test()
