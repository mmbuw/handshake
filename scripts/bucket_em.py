import sys
import json
import matplotlib.pyplot as plt
import itertools


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
	p_id = ""
	shake1 = ""
	shake2 = ""
	bucketdiff = 0

	def __init__(self, shake1, shake2) :
		self.bucketdiff = abs(shake1.bucket-shake2.bucket)

# maybe: Not Same Person

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

if __name__ == '__main__':

	max_value = 800
	number_of_buckets = 8	#maybe depending on Input
	window_size = 65		#maybe depending on Input

	#List of all handshakes
	handshakes = []
	#List of all valid pairs
	pairs = []
	#List of all fake pairs
	fakepairs = []

	data_json = sys.argv[1]
	with open(data_json) as data_file:    
	    data = json.load(data_file)
	shake_ids = data[str(window_size)].keys()

	#creates a List of all single sides of the handshakes
	i = 0
	for ids in shake_ids:
		value = data["65"][ids]["fft_y"][2]
		handshakes.append(Handshake(ids, getbucket(value,number_of_buckets,max_value)))

	#creates all possible pairs
	shake_id_pairs = list(itertools.combinations(handshakes, 2))

	#orders shakes into valid pairs and false pairs
	for pair in shake_id_pairs:
		if is_same_shake(pair):
			pairs.append(Pair(pair[0],pair[1]))
		else:
			fakepairs.append(Pair(pair[0],pair[1]))

	truehisto = []
	for x in pairs:
		truehisto.append(x.bucketdiff)

	falsehisto = []
	for x in fakepairs:
		falsehisto.append(x.bucketdiff)

	plt.hist(truehisto,bins=number_of_buckets, range=(0,number_of_buckets), normed = True, alpha=0.5, label='true')
	plt.hist(falsehisto,bins=number_of_buckets, range=(0,number_of_buckets), normed = True, alpha=0.5, label='false')
	plt.legend(loc='upper right')
	plt.show()

###### reminders
#fft_y2 = [x["fft_y"][2] for x in data["65"].values()]
