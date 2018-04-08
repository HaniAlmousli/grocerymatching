import pdb
from flask import Flask, jsonify
from flask import abort
from flask import send_file
from flask import request
from PIL import Image
import sys
import os
import io
sys.path.append(os.getcwd()+"/../model/")
from imagematch import ImageMatchingEngine


matching_engine = ImageMatchingEngine()

img= Image.open('/tmp/del.jpg','r')
        
imgByteArr = io.BytesIO()
img.save(imgByteArr, format='JPEG')
print(matching_engine.find_similar(imgByteArr.getvalue()))