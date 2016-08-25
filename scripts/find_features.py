import fft_utils
import file_utils
import math
import os
import sys
import itertools
import numpy as np
import matplotlib.pyplot as plt
import json
import scipy.stats as stats
from sklearn import tree
from sklearn import cross_validation
from sklearn import datasets
from sklearn.ensemble import ExtraTreesClassifier
from sklearn.metrics import confusion_matrix
from sklearn.externals.six import StringIO
from os import listdir
from os.path import isfile, join
from subprocess import call
from pylab import setp, hold, xlim, ylim

'''
Finds the most important features by iterating through the data and calculating pearson's return

Usage: python find_features.py ../data/shakes.json ../out_dir
'''


window_size = 0
feature_names = {}
input_data = {}

def get_instance_from_pair(shake1, shake2):
	feature_values = np.array([])
	for feature_name in sorted(shake1.keys()):
		feature_difference = difference_elementwise(shake1[feature_name], shake2[feature_name])
		feature_difference = feature_difference[:15]
		feature_values = np.concatenate((feature_values, feature_difference))

	return feature_values

def difference_elementwise(lista, listb):
	return [abs(a-b) for a,b in zip(lista,listb)]

def magnitude(vector):
	magnitude = 0.0
	for entry in vector:
		magnitude += entry * entry
	return math.sqrt(magnitude)

def get_data_and_target():

	shakes = input_data[str(window_size)]
	shake_ids = input_data[str(window_size)].keys()

	shake_id_pairs = list(itertools.combinations(shake_ids, 2))

	data = []
	target = []
	for pair in shake_id_pairs:
		if is_same_device(pair[0], pair[1]):
			continue
		if is_same_person(pair[0], pair[1]):
			continue
		instance_target = get_target(pair[0], pair[1])
		instance_values = get_instance_from_pair(shakes[pair[0]], shakes[pair[1]])

		#if instance_target == 1:
			#for x in range(190):
				#data.append(instance_values)
				#target.append(instance_target)

		data.append(instance_values)
		target.append(instance_target)

	return (data, target)

def get_target(shake_id1, shake_id2):
	timestamp1 = int(shake_id1.split('-')[0])
	timestamp2 = int(shake_id2.split('-')[0])
	if abs(timestamp1 - timestamp2) < 3:
		return 1
	else:
		return -1

def is_same_device(shake_id1, shake_id2):
	device_id1 = shake_id1.split('-')[1]
	device_id2 = shake_id2.split('-')[1]
	return device_id1 == device_id2

def is_same_person(shake_id1, shake_id2):
	try:
		name1 = shake_id1.split('-')[2].split('.')[0].strip().lower()
	except IndexError:
		return False
	try:
		name2 = shake_id2.split('-')[2].split('.')[0].strip().lower()
	except IndexError:
		return False
	return name1==name2

def setBoxColors(bp):
    setp(bp['boxes'][0], color='blue')
    setp(bp['caps'][0], color='blue')
    setp(bp['caps'][1], color='blue')
    setp(bp['whiskers'][0], color='blue')
    setp(bp['whiskers'][1], color='blue')
    setp(bp['fliers'][0], color='blue')
    setp(bp['fliers'][1], color='blue')
    setp(bp['medians'][0], color='blue')

    setp(bp['boxes'][1], color='red')
    setp(bp['caps'][2], color='red')
    setp(bp['caps'][3], color='red')
    setp(bp['whiskers'][2], color='red')
    setp(bp['whiskers'][3], color='red')
    setp(bp['fliers'][2], color='red')
    setp(bp['fliers'][3], color='red')
    setp(bp['medians'][1], color='red')

def get_feature_names(shake):
	feature_names = []
	for key in sorted(shake.keys()):
		if len(shake[key]) > 1:
			limit = min(15, len(shake[key]))
			for i in range(0, limit):
				feature_names.append(key+"_"+str(i))
		else:
			feature_names.append(key)

	return feature_names

def createBoxplot(file_name, important_attributes, data, target):

	ticks = []
	boxplot_data = []
	attr = []
	for (value, name, idx) in important_attributes:
		ticks.append(name+"\n"+str(value));
		values = []
		nope_values = []
		for i, row in enumerate(data):
			if target[i] == 1:
				values.append(row[idx])
			else:
				nope_values.append(row[idx])
		attr = [values, nope_values]
		boxplot_data.append(attr)

	# Create a figure instance
	fig = plt.figure(1, figsize=(16, 9))

	# Create an axes instance
	ax = fig.add_subplot(111)

	hold(True)

	stepsize = 4
	# Create the boxplot
	attr_len=len(boxplot_data)
	for i in range(0,attr_len):
		bp = ax.boxplot(boxplot_data[i], positions = [(stepsize*(i+1))-0.8, (stepsize*(i+1))+0.8], widths = 0.8)
		setBoxColors(bp)

	# set axes limits and labels and stepsize
	xlim(0,stepsize*(attr_len+1))
	ylim(0,800)
	ax.set_xticklabels(ticks)
	ax.set_xticks(range(stepsize,(attr_len+1)*stepsize,stepsize))

	## Remove top axes and right axes ticks
	ax.get_xaxis().tick_bottom()
	ax.get_yaxis().tick_left()

	# Increase font size
	plt.rcParams.update({'font.size': 22})

	# Save the figure
	fig.savefig(out_dir+'/'+file_name, bbox_inches='tight')
	plt.close(fig)
	print "created boxplot"

def writeArffFile(data, target, file_name):
	with open(file_name, 'w') as f:
		f.write('@RELATION handshake_matching\n')

		for feature_name in feature_names:
			f.write('@ATTRIBUTE '+feature_name+' REAL\n')

		f.write('@ATTRIBUTE class {-1,1}\n')
		f.write('@DATA\n')

		for idx, instance in enumerate(data):
			f.write(','.join(map(str, instance))+','+str(target[idx])+' \n')

def printTree(clf, file_name):
	class_names = ["no_match", "match"]
	with open("clf.dot", 'w') as f:
		f = tree.export_graphviz(
				clf,
				out_file = f,
				class_names = class_names,
				feature_names= feature_names
			)

	call(['dot', '-Tpdf', 'clf.dot', '-o', file_name])

def learn():

	print "learning with window size: %d" % window_size

	(data, target) = get_data_and_target()

	sum_of_shakes = len([1 for x in target if x == 1])
	sum_of_non_shakes = len(target) - sum_of_shakes

	print "sum_of_shakes: " + str(sum_of_shakes)
	print "sum_of_non_shakes: " + str(sum_of_non_shakes)

#	clf = tree.DecisionTreeClassifier(
#		criterion="entropy"
#	)
#
#	clf = clf.fit(data, target)
#
#	predicted = clf.predict(data)
#
#	cm = confusion_matrix(target, predicted)
#	print cm
#	print
#
#	scores = cross_validation.cross_val_score(clf, data, target, cv=4)
#	cv_mean = np.mean(scores)*100
#	print  "cross validation mean:\t"+str(np.mean(scores))
#	print
#
#	model = ExtraTreesClassifier()
#	model.fit(data, target)
#	# display the relative important_attributes of each attribute


	print "important attributes:"
	cv_mean = "NA"
	number_of_features = len(data[0])
	important_attributes = []
	for i in range(number_of_features):
		feature_vector = [instance[i] for instance in data]
		importance = abs(stats.pearsonr(feature_vector, target)[0])
		important_attributes.append((importance, feature_names[i], i))

	#important_attributes = [ (val*100, feature_names[i], i) for i, val in enumerate(model.feature_importances_)]

	important_attributes.sort(reverse=True)
	correlation = important_attributes[0][0] + important_attributes[1][0]
	for (val, feature_name, idx) in important_attributes[0:100]:
		print "%s : %.2f" % (feature_name, val)

	choosen_attributes = []
	for (val, feature_name, idx) in important_attributes[0:4]:
		choosen_attributes.append(("%.2f" % val, feature_name, idx))

	print

	file_name = "%.4f_%02d" % (correlation, window_size) 

	#writeArffFile(data, target, file_name + ".arff")

	#printTree(clf, file_name + ".pdf")

	print

	createBoxplot(file_name+"boxplot.png", choosen_attributes, data, target)

if __name__ == '__main__':

	json_file = sys.argv[1]
	out_dir = sys.argv[2]

	with open(json_file) as data_file:
		global input_data
		input_data = json.load(data_file)

	for new_window_size in range(50, 105, 5):
		example_shake = input_data[str(new_window_size)].itervalues().next()
		print example_shake
		global window_size
		window_size = new_window_size
		global feature_names
		feature_names = get_feature_names(example_shake)
		learn()