import simplejson as json

import flask
import pydantic as p
import numpy as np
import pandas as pd


class JsonEncoder(json.JSONEncoder):
    def __init__(self, **kwargs):
        kwargs.setdefault('ensure_ascii', False)
        super().__init__(**kwargs)

    def default(self, obj):
        if isinstance(obj, p.BaseModel):
            return obj.dict()
        if isinstance(obj, np.integer):
            return int(obj)
        if isinstance(obj, np.floating):
            return float(obj)
        # if isinstance(obj, np.ndarray):
        #     return obj.tolist()
        # if isinstance(obj, float) and np.isnan(obj):
        #     return "null"
        if isinstance(obj, pd.Series):
            return obj.to_list()
        return super().default(obj)


class JsonResponse(flask.Response):
    default_mimetype = 'application/json'


def make_json_response(obj, status: int = 200):
    """Make a JSON response. Has special approach for Pydantic models.

    Args:
        obj (pydantic.BaseModel | object): the object to be JSON serialized as response body
        status (int): the response status code

    Returns:
        JsonResponse: the JSON response
    """
    body = json.dumps(obj, cls=JsonEncoder, ignore_nan=True)
    return JsonResponse(response=body, status=status)


def make_err_response(error: str, status: int):
    """Generate an error JSON response according to our agreed error format.
    """
    return make_json_response({'error': error}, status=status)


def make_404_response(error: str ='The object was not found'):
    return make_err_response(error, 404)


def abort(data, status: int):
    """Immediately return a response.
    """
    flask.abort(make_json_response(data, status=status))
