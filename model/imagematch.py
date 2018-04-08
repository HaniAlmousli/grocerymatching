import tensorflow as tf
import numpy as np
import os
import re
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from scipy import ndimage
import io
from PIL import Image
import pdb
import inspect
class ImageMatchingEngine(object):

    def __init__(self):
        self.model_dir =  os.path.abspath('/'.join(inspect.getmodule(self).__file__.split('/')[0:-1]))+'/'
        # pdb.set_trace()
        self.images_dir = 'Groceries/'
        list_images = [f for f in os.listdir(self.images_dir) if re.search('jpg|JPG', f)]
        self.list_images = np.sort(list_images)
        self.fill_db()
        self.create_graph()
        self.extract_features()

    def fill_db(self):
        #Attach every image to the right index that has its info
        f= open(self.images_dir+'imageIndex.csv')
        imageIndexData = f.read().split('\n')[0:-1]
        f.close()
        self.imageIndexDB={} # name is key, index is value which refers to 
        for l in imageIndexData:
            name,index= l.split(',')
            self.imageIndexDB[name]=int(index)

        #Load indices with the needed info (e.g type)
        f= open(self.images_dir+'indexInfo.csv')
        indexInfoData = f.read().split('\n')[0:-1]
        f.close()
        self.indexInfo={}
        self.indexPrice={}
        for line in indexInfoData:
            index,prodName,price = line.split(',')
            self.indexInfo[index]=prodName
            self.indexPrice[index]=price
        # pdb.set_trace()


    # 2012 Inception-v3. 
    def create_graph(self):
        with tf.gfile.FastGFile(os.path.join(self.model_dir, 'classify_image_graph_def.pb'), 'rb') as f:
            graph_def = tf.GraphDef()
            graph_def.ParseFromString(f.read())
            _ = tf.import_graph_def(graph_def, name='')

    def extract_features(self):
        print("Processing Images....")
        nb_features = 2048
        features = np.empty((len(self.list_images),nb_features))
        with tf.Session() as sess:
            next_to_last_tensor = sess.graph.get_tensor_by_name('pool_3:0')
            for ind, image in enumerate(self.list_images):
                if not tf.gfile.Exists(self.images_dir+image):
                    tf.logging.fatal('File does not exist %s', self.images_dir+image)
                image_data = tf.gfile.FastGFile(self.images_dir+image, 'rb').read()
                predictions = sess.run(next_to_last_tensor,{'DecodeJpeg/contents:0': image_data})
                features[ind,:] = np.squeeze(predictions)
        self.features=features
        print("------------->>> Processing done <<<-------------")


    def extract_image_feature(self,img):
        nb_features = 2048
        features = np.empty((1,nb_features))
        with tf.Session() as sess:
            next_to_last_tensor = sess.graph.get_tensor_by_name('pool_3:0')
            image_data = img
            predictions = sess.run(next_to_last_tensor,{'DecodeJpeg/contents:0': image_data})
            features[0,:] = np.squeeze(predictions)
        return features

    def find_similar(self,byteImage):
        res = self.extract_image_feature(byteImage)
        indices = np.argsort(np.sum((res-self.features)**2,axis=1))
        image_file_name = self.list_images[indices[0]]
        index = self.imageIndexDB[image_file_name]
        imtype = self.indexInfo[str(index)]
        imprice = self.indexPrice[str(index)]
        return self.images_dir+image_file_name,imtype,imprice
    # features = extract_features(list_images)          
