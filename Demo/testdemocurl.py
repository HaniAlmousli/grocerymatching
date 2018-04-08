import pdb
from flask import Flask, jsonify
from flask import abort
from flask import send_file
from flask import request
import requests,json
from PIL import Image
import sys
import os
import io
sys.path.append(os.getcwd()+"/../model/")

IP_ADDRESS="23.233.47.6"

img= Image.open('/tmp/del.jpg','r')        
imgByteArr = io.BytesIO()
img.save(imgByteArr, format='JPEG')

# curl_response = requests.post('http://localhost:5000/groceries/api/v1.0/getsimilar', 
#     files= {'file':imgByteArr.getvalue()})
# pdb.set_trace()
curl_response = requests.post('http://'+IP_ADDRESS+':5000/groceries/api/v1.0/getsimilar', 
    files= {'file':imgByteArr.getvalue()})

pdb.set_trace()
jsnv = curl_response.json()['img']
# bytesA = [bytes([int(i)]) for i in jsnv[1:-1].split(',')]
# final = b''.join(bytesA)
exec('final=bytes('+jsnv+')')
im = Image.open(io.BytesIO(final))
im.show()
"""
coming from phone
data=request.files['file'].stream.read()
"""