#!/home/hani/anaconda3/bin/python
import pdb
from flask import Flask, jsonify
from flask import abort
from flask import send_file
from flask import request
from PIL import Image
from importlib.machinery import SourceFileLoader
import sys
import os
import io
sys.path.append(os.getcwd()+"/../model/")
from imagematch import ImageMatchingEngine 
import pdb
import json

IP_ADDRESS=' . . . '

app = Flask(__name__)
matching_engine= None




@app.route("/groceries/api/v1.0/getsimilar", methods=["POST","GET"])
def find_similar():
    """
    TAKE AN IMAGE FROM CLIENT, ANALYZE IT AND RETURN NEW IMAGE
    curl -F "file=@/home/0.bmp"  http://localhost:5000/todo/api/v1.0/send
    curl -F "file=@/tmp/del.jpg"  http://localhost:5000/groceries/api/v1.0/getsimilar
    """
    global matching_engine
    if matching_engine is None: 
        matching_engine = ImageMatchingEngine()


    data = request.files['file'].stream.read()
    img = Image.open(io.BytesIO(data))
    imgByteArr = io.BytesIO()
    img.save(imgByteArr, format='JPEG')
    imPath,prodType,prodPrice = matching_engine.find_similar(imgByteArr.getvalue())

    print(imPath,prodType,prodPrice)
    imgRes = Image.open(imPath)
    imgByteArrRes = io.BytesIO()
    imgRes.save(imgByteArrRes, format='JPEG')
    return jsonify({'type':prodType,'price':float(prodPrice)})
    
if __name__ == '__main__':
    app.run(host='0.0.0.0')#debug=True,
