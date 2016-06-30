import fft_utils
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
import matplotlib.pyplot as plt

feature_names = []

fft_y_0 = []
fft_y_2 = []
fft_y_1 = []
fft_magnitude_0 = []
fft_yz_3 = []
fft_magnitude_3 = []
fft_yz_0 = []
fft_z_6 = []
fft_xy_1 = []
fft_yz_2 = []
fft_z_0 = []

def get_instance_from_pair(pair):
	file1 = pair[0]
	file2 = pair[1]

	target = get_target(file1, file2)

	val1 = [float(x) for x in fft_utils.load_values_from_file(file1, 2)]
	val2 = [float(x) for x in fft_utils.load_values_from_file(file2, 1)]

	values = np.array([])
	fft_x = fft_utils.get_fft_difference(file1, file2, 0)
	values = np.concatenate((values, fft_x))

	global feature_names
	if len(feature_names) == 0:
		for i in range(len(values)):
			feature_names.append("fft_x_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_y_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_z_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_xy_"+str(i))
		for i in range(len(values)):
			feature_names.append("fft_xz_"+str(i))			
		for i in range(len(values)):
			feature_names.append("fft_yz_"+str(i))
		for i in range(len(values)):
			feature_names.append("magnitude_"+str(i))		
		feature_names.append("mean")
		feature_names.append("range")
		feature_names.append("zero_cross")
	

	fft_y = fft_utils.get_fft_difference(file1, file2, 1)
	values = np.concatenate((values, fft_y))

	fft_z = fft_utils.get_fft_difference(file1, file2, 2)
	values = np.concatenate((values, fft_z))

	fft_xy = fft_utils.get_fft_xy_difference(file1, file2)
	values = np.concatenate((values, fft_xy))

	fft_xz  = fft_utils.get_fft_xz_difference(file1, file2)
	values = np.concatenate((values, fft_xz))

	fft_yz = fft_utils.get_fft_yz_difference(file1, file2)
	values = np.concatenate((values, fft_yz))

	fft_magnitude = fft_utils.get_fft_magnitude_difference(file1, file2)
	values = np.concatenate((values, fft_magnitude))

	if target == 1:
		fft_y_0.append(fft_y[0])
		fft_y_2.append(fft_y[2])
		fft_y_1.append(fft_y[1])
		fft_magnitude_0.append(fft_magnitude[0])
		fft_yz_3.append(fft_yz[3])
		fft_magnitude_3.append(fft_magnitude[3])
		fft_yz_0.append(fft_yz[0])
		fft_z_6.append(fft_z[6])
		fft_xy_1.append(fft_xy[1])
		fft_yz_2.append(fft_yz[2])
		fft_z_0.append(fft_z[0])
		#print "fft_y[2]: "+str(fft_y[2])
		#print "fft_y[1]: "+str(fft_y[1])
		#print "fft_magnitude[0]: "+str(fft_magnitude[0])
		#print "fft_yz[3]: "+str(fft_yz[3])
		#print "fft_magnitude[3]: "+str(fft_magnitude[3])
		#print "fft_yz[0]: "+str(fft_yz[0])
		#print "fft_z[6]: "+str(fft_z[6])
		#print "fft_xy[1]: "+str(fft_xy[1])
		#print "fft_yz[2]: "+str(fft_yz[2])
		#print "fft_z[0]: "+str(fft_z[0])
		#print "#######################"
		#print


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
		#if is_signal_too_short(pair[0], pair[1]):
			#continue
		instance_values, instance_target = get_instance_from_pair(pair)

		if instance_target == 1:
			for x in range(20):
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

def is_signal_too_short(file1, file2):
	x = len(load_values_from_file(file1, 0))
	y = len(load_values_from_file(file2, 0))
	return min(x,y) < 80

def createBoxplot():
	boxplot_data = [
				fft_y_0,
				fft_y_2,
				fft_y_1,
				fft_magnitude_0,
				fft_yz_3,
				fft_magnitude_3,
				fft_yz_0,
				fft_z_6,
				fft_xy_1,
				fft_yz_2,
				fft_z_0,
			]

	ticks = [
				"fft_y_0",
				"fft_y_2",
				"fft_y_1",
				"fft_magnitude_0",
				"fft_yz_3",
				"fft_magnitude_3",
				"fft_yz_0",
				"fft_z_6",
				"fft_xy_1",
				"fft_yz_2",
				"fft_z_0",
			]

	# Create a figure instance
	fig = plt.figure(1, figsize=(16, 9))

	# Create an axes instance
	ax = fig.add_subplot(111)

	# Create the boxplot
	bp = ax.boxplot(boxplot_data)

	ax.set_xticklabels(ticks)

	## Remove top axes and right axes ticks
	ax.get_xaxis().tick_bottom()
	ax.get_yaxis().tick_left()

	# Save the figure
	fig.savefig('boxplot.png', bbox_inches='tight')
	print "created boxplot"


def main():
	(data, target) = get_data_and_target()
	createBoxplot()

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