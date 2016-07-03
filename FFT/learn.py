from fft_utils import get_fft_difference, load_values_from_file
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

positive_samples = 0
negative_samples = 0
feature_names = []

def get_instance_from_pair(pair):
	file1 = pair[0]
	file2 = pair[1]

	target = get_target(file1, file2)

	if target == 1:
		global positive_samples
		positive_samples+=1
	else:
		global negative_samples
		negative_samples+=1

	val1 = [float(x) for x in load_values_from_file(file1, 2)]
	val2 = [float(x) for x in load_values_from_file(file2, 1)]
	
	values = np.array([])
	values = np.concatenate((values, get_fft_difference(file1, file2, 10000, 0)))

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

	values = np.concatenate((values, get_fft_difference(file1, file2, 10000, 1)))
	#print len(values)
	values = np.concatenate((values, get_fft_difference(file1, file2, 10000, 2)))
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
	(data, target) = get_data_and_target()
	global positive_samples,negative_samples
	print "positive samples:\t"+str(positive_samples)
	print "negative samples:\t"+str(negative_samples)

	with open("data.arff", 'w') as f:
		f.write('@RELATION handshake_matching\n')

		for feature_name in feature_names:
			f.write('@ATTRIBUTE '+feature_name+' REAL\n')

		f.write('@ATTRIBUTE class {-1,1}\n')
		f.write('@DATA\n')

		for idx, instance in enumerate(data):
			f.write(','.join(map(str, instance))+','+str(target[idx])+'\n')


	clf = tree.DecisionTreeClassifier(
		criterion="entropy"
	)
	clf = clf.fit(data, target)

	predicted = clf.predict(data)

	cm = confusion_matrix(target, predicted)

	print cm

	scores = cross_validation.cross_val_score(clf, data, target, cv=4)
	print  "cross validation mean:\t"+str(np.mean(scores))

	class_names = ["no_match", "match"]

	with open("clf.dot", 'w') as f:
		f = tree.export_graphviz(
				clf,
				out_file = f,
				class_names = class_names,
				feature_names= feature_names
			)

	call(['dot', '-Tpdf', 'clf.dot', '-o', 'tree.pdf'])

if __name__ == '__main__':
	main()