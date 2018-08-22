## Installation

1. Make sure [Python 3.6+](https://www.python.org/downloads/) is installed. 
2. Install [pipenv](https://github.com/kennethreitz/pipenv). 

    ```
    $ pip install pipenv 
    ```

3. Create a _virtual environment_ and specify the Python version to use. 

    ```
    $ pipenv --python=python3.6
    ```

4. Install requirements.  

    ```
    $ pipenv install 
    ``` 

5. Run the server:
    * `$ pipenv run python node.py` 
    * `$ pipenv run python node.py -p 5001`
    * `$ pipenv run python node.py --port 5002`
    
6. Run the client:

    ```
    $ python client.py
    ``` 
