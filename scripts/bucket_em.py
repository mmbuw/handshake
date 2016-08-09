import sys
import json
import matplotlib.pyplot as plt
import itertools
import numpy as np


class Handshake(object):
	#id = filename  
	#bucket = bucket of fft_x_2 value
	h_id = ""
	bucket = 0

	def __init__(self, h_id, bucket):
		self.h_id = h_id
		self.bucket = bucket

class Pair(object):
	#p_id = (unused)
	#shake1 and shake2 = the shakes in the pair (unused)
	#bucketdiff = bucket difference of both pairs
	#status 0=truePositve, 1=falseNegative, 2=falsePositive, 3=trueNegative
	p_id = ""
	shake1 = ""
	shake2 = ""
	bucketdiff = 0
	status = 0 

	def __init__(self, shake1, shake2, status) :
		self.shake1=shake1
		self.shake2=shake2
		self.bucketdiff = abs(shake1.bucket-shake2.bucket)
		self.status = status

	def updateBuckets(self, bucket1, bucket2):
		self.shake1.bucket=bucket1
		self.shake2.bucket=bucket2
		self.bucketdiff=abs(bucket1-bucket2)

# !!Not Same Person!!

def is_same_person(pair):
	try:
		name1 = pair[0].h_id.split('-')[2].split('.')[0].strip().lower()
	except IndexError:
		return False
	try:
		name2 = pair[1].h_id.split('-')[2].split('.')[0].strip().lower()
	except IndexError:
		return False

	return name1==name2


def is_same_shake(pair):
	timestamp1 = int(pair[0].h_id.split('-')[0])
	timestamp2 = int(pair[1].h_id.split('-')[0])
	if abs(timestamp1 - timestamp2) < 3:
		return True
	else:
		return False

def getbucket(value,num,max_value):
	bucketsize = max_value / num
	return int(value/bucketsize)

def buildPairs(data, window_size, category, categoryValue , max_value, number_of_buckets=8):
	handshakes = []

	shake_ids = data[str(window_size)].keys()

	#creates a List of all handshakes
	for ids in shake_ids:
		value = data[window_size][ids][category][categoryValue]
		handshakes.append(Handshake(ids, getbucket(value,number_of_buckets,max_value)))

	#creates all possible pairs
	shake_id_pairs = list(itertools.combinations(handshakes, 2))

	#orders shakes into valid pairs and false pairs
	for pair in shake_id_pairs:
		if is_same_shake(pair):
			pairs.append(Pair(pair[0],pair[1],0))
		elif not is_same_person(pair):
			pairs.append(Pair(pair[0],pair[1],2))


	return pairs

def updatePairs(pairs, window_size, category, categoryValue, max_value, number_of_buckets=8):
	for x in pairs:
		ids1 = x.shake1.h_id
		ids2 = x.shake2.h_id
		value1 = data[window_size][ids1][category][categoryValue]
		value2 = data[window_size][ids2][category][categoryValue]
		bucket1 = getbucket(value1,number_of_buckets,max_value)
		bucket2 = getbucket(value2,number_of_buckets,max_value)
		x.updateBuckets(bucket1,bucket2)	
	return pairs

def filterPairs(pairs, difftolerance=1):
	for x in pairs:
		if x.bucketdiff > difftolerance:
			if x.status == 0 or x.status == 2:
				x.status = x.status+1
	return pairs

def drawHisto(pairs, number_of_buckets=8):
	truePos =[]
	normedLenPos = 0
	falsePos=[]
	normedLenNeg = 0
	for x in pairs:
		if x.status==0:
			truePos.append(x.bucketdiff)
			normedLenPos += 1
		elif x.status==1:
		## to visualize false negative they are visualized in bucket diff = 7 to keep the normalisation
			truePos.append(number_of_buckets-2) 
		elif x.status==2:
			falsePos.append(x.bucketdiff)
			normedLenNeg += 1
		else:
		## to visualize true negatives they are visualized in bucket diff = 7 to keep the normalisation
			falsePos.append(number_of_buckets-2) 

	hist, bins = np.histogram(truePos, range=(0,number_of_buckets), bins=number_of_buckets, density=True)
	widths = np.diff(bins)
	#hist = hist*normedLenPos* (1/float(len(truePos)))
	plt.bar(bins[:-1], hist, widths, alpha=0.5, color='g')

	hist, bins = np.histogram(falsePos, range=(0,number_of_buckets), bins=number_of_buckets, density=True)
	widths = np.diff(bins)
	#hist = hist*normedLenNeg* (1/float(len(falsePos)))
	plt.bar(bins[:-1], hist, widths, alpha=0.5, color='r')

	plt.ylim([0,1])
	plt.xlim([0,number_of_buckets-1])
	plt.show()


if __name__ == '__main__':

	#maybe depending on Input
	max_value = 800
	number_of_buckets = 8	
	window_size = "65"		
	category = "fft_y"
	categoryValue = 2
	difftolerance = 1 	#a tolerance of x will include all diffs from 0 to x

	#List of all valid pairs
	pairs = []
	#List of all fake pairs
	fakepairs = []
	#Analysis Lists
	truePositive = []
	falseNegative= []
	trueNegative = []
	falsePositive= []

	#getting the data from the json file
	data_json = sys.argv[1]
	with open(data_json) as data_file:    
	    data = json.load(data_file)


	handshakes = buildPairs(data, window_size, category, categoryValue ,max_value, number_of_buckets)

	drawHisto(pairs, number_of_buckets)

	handshakes = filterPairs(pairs, difftolerance)

	drawHisto(pairs, number_of_buckets)

	max_value = 800
	number_of_buckets = 8	
	window_size = "65"		
	category = "fft_y"
	categoryValue = 3
	difftolerance = 1 

	handshakes = updatePairs(pairs, window_size, category, categoryValue, max_value, number_of_buckets)

	drawHisto(pairs, number_of_buckets)

	handshakes = filterPairs(pairs, difftolerance)

	drawHisto(pairs, number_of_buckets)


###### reminders
#fft_y2 = [x["fft_y"][2] for x in data["65"].values()]
