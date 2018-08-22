import requests


class Node:
    def __init__(self, url='http://0.0.0.0:5000', name=''):
        self.url = url
        self.name = name.lower()
        resp = requests.get(self.url + '/id')
        self.id = resp.json()['id']

    def __call__(self, *args, **kwargs):
        resp = requests.get(self.url + '/chain')
        return 'GET /chain ' + str(resp.status_code) + "\n" + resp.text

    def mine(self):
        resp = requests.get(self.url + '/mine')
        return 'GET /mine ' + str(resp.status_code) + "\n" + resp.text

    def register(self, nodes):
        resp = requests.post(self.url + '/nodes/register',
                             json={'nodes': nodes})
        return 'POST /nodes/register ' + str(resp.status_code) + "\n" + resp.text

    def consensus(self):
        resp = requests.get(self.url + '/nodes/resolve')
        return 'GET /nodes/resolve ' + str(resp.status_code) + "\n" + resp.text

    def transaction(self, sender, recipient, amount):
        resp = requests.post(self.url + '/transactions/new',
                             json={'sender': sender, 'recipient': recipient, 'amount': amount})
        return 'POST /transactions/new ' + str(resp.status_code) + "\n" + resp.text

    def trace(self, recipient, amount):
        resp = requests.post(self.url + '/transactions/trace',
                             json={'recipient': recipient, 'amount': amount})
        return 'POST /transactions/trace ' + str(resp.status_code) + "\n" + resp.text

    def __str__(self):
        return self.name


class Cluster:
    def __init__(self):
        self.nodes = []

    def append(self, new_node):
        if self.check_repeated(new_node):
            return
        neighbours = []
        for node in self.nodes:
            neighbours.append(node.url)
            print(str(node) + ' ' + node.register([new_node.url]))
        if neighbours:
            print(str(new_node) + ' ' + new_node.register(neighbours))
        self.nodes.append(new_node)

    def mine(self, name):
        for node in self.nodes:
            if str(node) == name:
                print(str(node) + ' ' + node.mine())
                self.consensus()
                break

    def transaction(self, sender_name, recipient_name, amount):
        if sender_name == recipient_name:
            return
        for node in self.nodes:
            if str(node) == sender_name:
                sender = node.id
                sender_node = node
            if str(node) == recipient_name:
                recipient = node.id
        if sender and recipient and sender_node and sender != recipient:
            print(str(sender_node) + ' ' + sender_node.transaction(sender, recipient, amount))
            self.consensus()

    def show(self, name):
        for node in self.nodes:
            if str(node) == name:
                print(str(node) + ' ' + node())
                break

    def trace(self, name, amount):
        for node in self.nodes:
            if str(node) == name:
                print(str(node) + ' ' + node.trace(node.id, amount))
                break

    def check_repeated(self, new_node):
        for node in self.nodes:
            if str(new_node) == str(node) or new_node.url == node.url or new_node.id == node.id:
                return True
        return False

    def consensus(self):
        for node in self.nodes:
            print(str(node) + ' ' + node.consensus())


def test():
    cluster = Cluster()
    cluster.append(Node('http://0.0.0.0:5000', 'user1'))
    # cluster.append(Node('http://0.0.0.0:5001', 'user2'))
    # cluster.append(Node('http://0.0.0.0:5002', 'user3'))
    cluster.mine('user1')
    cluster.mine('user1')
    # cluster.mine('user2')
    # cluster.show('user1')
    # cluster.show('user2')
    # cluster.transaction('user1', 'user2', 1)
    # cluster.mine('user1')
    # cluster.trace('user2', 1)


if __name__ == "__main__":
    test()
