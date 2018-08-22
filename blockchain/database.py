from sqlalchemy import Column, DateTime, Integer, PickleType, String, create_engine
from sqlalchemy.ext.declarative import declarative_base, declared_attr
from sqlalchemy.orm import scoped_session, sessionmaker
from uuid import uuid4

u_id = uuid4().hex
engine = create_engine(f'sqlite:///data/{u_id}.db')
product_engine = create_engine(f'sqlite:///product.db')
db = scoped_session(sessionmaker(bind=engine))
Base = declarative_base()


class BaseModel(Base):
    __abstract__ = True

    @declared_attr
    def __tablename__(self):
        return self.__name__.lower()

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class Peer(BaseModel):
    identifier = Column(String(32), primary_key=True)
    hostname = Column(String, index=True, unique=True)
    timestamp = Column(DateTime, index=True)


class Block(BaseModel):
    height = Column(Integer, primary_key=True)
    timestamp = Column(DateTime, index=True)
    transactions = Column(PickleType)
    previous_hash = Column(String(64))
    proof = Column(String(64))
    hash = Column(String(64))


class Product(BaseModel):
    product_id = Column(String(32), primary_key=True)
    creator_id = Column(String(32))
    status = Column(String(64))
    description = Column(PickleType)


class Config(BaseModel):
    key = Column(String(64), primary_key=True, unique=True)
    value = Column(PickleType)


def reset_db():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    session_factory = sessionmaker(bind=engine)
    session = session_factory()
    session.add(Config(key='node_identifier', value=u_id))
    session.commit()
    session.close()
