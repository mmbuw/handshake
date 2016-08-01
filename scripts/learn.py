import fft_utils
import file_utils
import math
from sklearn import tree
from sklearn import datasets
from sklearn.externals.six import StringIO
from sklearn.ensemble import ExtraTreesClassifier
import os
import sys
from os import listdir
from os.path import isfile, join
import itertools
from sklearn import cross_validation
from sklearn.metrics import confusion_matrix
import numpy as np
from subprocess import call
import matplotlib.pyplot as plt
from pylab import setp, hold, xlim, ylim

feature_names = []
window_size = 70

def get_instance_from_pair(pair):
	file1 = pair[0]
	file2 = pair[1]

	target = get_target(file1, file2)

	values1 = [x for x in file_utils.load_values_from_file(file1, window_size)]
	x1 = [x[0] for x in values1]
	y1 = [x[1] for x in values1]
	z1 = [x[2] for x in values1]
	mag1 = [magnitude(x) for x in values1]

	values2 = [x for x in file_utils.load_values_from_file(file2, window_size)]
	x2 = [x[0] for x in values2]
	y2 = [x[1] for x in values2]
	z2 = [x[2] for x in values2]
	mag2 = [magnitude(x) for x in values2]

	feature_values = np.array([])
	fft_x = fft_utils.get_fft_difference(x1, x2)
	feature_values = np.concatenate((feature_values, fft_x))

	fft_y = fft_utils.get_fft_difference(y1, y2)
	feature_values = np.concatenate((feature_values, fft_y))

	fft_z = fft_utils.get_fft_difference(z1, z2)
	feature_values = np.concatenate((feature_values, fft_z))

	fft_xy = fft_utils.get_fft_difference(x1, y2)
	feature_values = np.concatenate((feature_values, fft_xy))

	fft_xz  = fft_utils.get_fft_difference(x1, z2)
	feature_values = np.concatenate((feature_values, fft_xz))

	fft_yz = fft_utils.get_fft_difference(y1, z2)
	feature_values = np.concatenate((feature_values, fft_yz))

	fft_magnitude = fft_utils.get_fft_difference(mag1, mag2)
	feature_values = np.concatenate((feature_values, fft_magnitude))

	for i in range(0,3):
		mean_difference = abs(np.mean(values1[i]) - np.mean(values2[i]))
		feature_values = np.concatenate((feature_values, [mean_difference]))

		ptp_aka_range = abs(np.ptp(values1[i]) - np.ptp(values2[i]))
		feature_values = np.concatenate((feature_values, [ptp_aka_range]))

		zero_crossings = abs(len( np.where( np.diff( np.sign( values1[i] ) ) )[0] ) - len( np.where( np.diff( np.sign( values2[i] ) ) )[0] ))
		feature_values = np.concatenate((feature_values, [zero_crossings]))

	global feature_names
	if len(feature_names) == 0:
		for i in range(len(fft_x)):
			feature_names.append("fft_x_"+str(i))
		for i in range(len(fft_x)):
			feature_names.append("fft_y_"+str(i))
		for i in range(len(fft_x)):
			feature_names.append("fft_z_"+str(i))
		for i in range(len(fft_x)):
			feature_names.append("fft_xy_"+str(i))
		for i in range(len(fft_x)):
			feature_names.append("fft_xz_"+str(i))			
		for i in range(len(fft_x)):
			feature_names.append("fft_yz_"+str(i))
		for i in range(len(fft_x)):
			feature_names.append("magnitude_"+str(i))		

		feature_names.append("mean_x")
		feature_names.append("range_x")
		feature_names.append("zero_cross_x")

		feature_names.append("mean_y")
		feature_names.append("range_y")
		feature_names.append("zero_cross_y")

		feature_names.append("mean_z")
		feature_names.append("range_z")
		feature_names.append("zero_cross_z")

	return (feature_values, target)

def magnitude(vector):
	magnitude = 0.0
	for entry in vector:
		magnitude += entry * entry
	return math.sqrt(magnitude)

def get_data_and_target():
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	data = []
	target = []
	for pair in txtfilepairs:
		if is_same_device(pair[0], pair[1]):
			continue
		if is_same_person(pair[0], pair[1]):
			continue
		instance_values, instance_target = get_instance_from_pair(pair)

		if instance_target == 1:
			for x in range(190):
				data.append(instance_values)
				target.append(instance_target)

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

def is_same_person(file1, file2):
	try:
		name1 = file1.split('-')[2].split('.')[0].strip().lower()
	except IndexError:
		return False
	try:
		name2 = file2.split('-')[2].split('.')[0].strip().lower()
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
	ylim(0,500)
	ax.set_xticklabels(ticks)
	ax.set_xticks(range(stepsize,(attr_len+1)*stepsize,stepsize))

	## Remove top axes and right axes ticks
	ax.get_xaxis().tick_bottom()
	ax.get_yaxis().tick_left()

	# Save the figure
	fig.savefig(file_name, bbox_inches='tight')
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

def learn(new_window_size):

	print "learning with window size: %d" % new_window_size

	window_size = new_window_size

	(data, target) = get_data_and_target()

	clf = tree.DecisionTreeClassifier(
		criterion="entropy"
	)

	clf = clf.fit(data, target)

	predicted = clf.predict(data)

	cm = confusion_matrix(target, predicted)
	print cm
	print

	scores = cross_validation.cross_val_score(clf, data, target, cv=4)
	cv_mean = np.mean(scores)*100
	print  "cross validation mean:\t"+str(np.mean(scores))
	print

	model = ExtraTreesClassifier()
	model.fit(data, target)
	# display the relative important_attributes of each attribute
	print "important attributes:"
	important_attributes = [ (val*100, feature_names[i], i) for i, val in enumerate(model.feature_importances_)]
	important_attributes.sort(reverse=True)
	choosen_attributes = []
	for (val, feature_name, idx) in important_attributes[0:10]:
		choosen_attributes.append(("%.2f" % val, feature_name, idx))
		print "%s : %.2f" % (feature_name, val)

	print

	file_name = "all_handshakes/%.2f_%02d" % (cv_mean, window_size) 

	writeArffFile(data, target, file_name + ".arff")

	printTree(clf, file_name + ".pdf")

	print

	createBoxplot(file_name+"boxplot.png", choosen_attributes, data, target)

if __name__ == '__main__':
	for x in range(5, 105, 5):
		learn(x)