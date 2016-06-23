from fft_utils import get_fft_difference, load_values_from_file, get_window_size
from sklearn import tree
from sklearn.datasets import load_iris
from sklearn.externals.six import StringIO
import os
import sys
from os import listdir
from os.path import isfile, join
import itertools
from sklearn import cross_validation
from sklearn.metrics import confusion_matrix
import numpy as np
from subprocess import call

feature_names = []

def get_instance_from_pair(pair):
	file1 = pair[0]
	file2 = pair[1]

	target = get_target(file1, file2)

	val1 = [float(x) for x in load_values_from_file(file1, 2)]
	val2 = [float(x) for x in load_values_from_file(file2, 1)]
	
	values = np.array([])
	values = np.concatenate((values, get_fft_difference(file1, file2, 0)))

	#print len(values)

	global feature_names
	if len(feature_names) == 0:
		for i in range(len(values)):
			feature_names.append("fft_x_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_y_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_z_"+str(i))
		feature_names.append("mean")
		feature_names.append("range")
		feature_names.append("zero_cross")

	values = np.concatenate((values, get_fft_difference(file1, file2, 1)))
	#print len(values)
	values = np.concatenate((values, get_fft_difference(file1, file2, 2)))
	#print len(values)

	mean_difference = abs(np.mean(val1) - np.mean(val2))
	values = np.concatenate((values, [mean_difference]))
	ptp_aka_range = abs(np.ptp(val1) - np.ptp(val2))
	values = np.concatenate((values, [ptp_aka_range]))
	zero_crossings = abs(len( np.where( np.diff( np.sign( val1 ) ) )[0] ) - len( np.where( np.diff( np.sign( val2 ) ) )[0] ))
	values = np.concatenate((values, [zero_crossings]))
	#print len(values)
	#print'#####'

	return (values, target)

def get_data_and_target():
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	data = []
	target = []
	for pair in txtfilepairs:
		if is_same_device(pair[0], pair[1]):
			continue
		instance_values, instance_target = get_instance_from_pair(pair)
		data.append(instance_values)
		target.append(instance_target)
	return (data, target)

def get_target(file1, file2):
	timestamp1 = int(file1.split('/')[-1].split('-')[0])
	timestamp2 = int(file2.split('/')[-1].split('-')[0])
	if abs(timestamp1 - timestamp2) < 3:
		return 1
	else:
		return -1

def is_same_device(file1, file2):
	device_id1 = file1.split('/')[-1].split('-')[1]
	device_id2 = file2.split('/')[-1].split('-')[1]
	return device_id1 == device_id2

def main():
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	z = 0

	for pair in txtfilepairs:
		target = get_target(pair[0], pair[1])
		if target == 1:
			print get_window_size(pair[0])
			print get_window_size(pair[1])
			print abs(get_window_size(pair[0]) - get_window_size(pair[1]))
			print (float(get_window_size(pair[0])) / float(get_window_size(pair[1])))
			print ("---------------------------------")
			if abs(1-(float(get_window_size(pair[0])) / float(get_window_size(pair[1]))))>=0.25:
				z += 1
	print z

if __name__ == '__main__':
	main()