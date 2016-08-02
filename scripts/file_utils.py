import sys
import itertools
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from scipy.signal import argrelextrema
from os import listdir
from os.path import isfile, join
import numpy as np

import matplotlib.pyplot as plt
import math
from math import factorial

def load_values_from_file(file_path, window_size):
	values = []
	with open(file_path, 'r') as f:
		lines = f.readlines()

		if lines[0].startswith('#'):
			meta_data = lines[0].split(', ')
			devicename = meta_data[0]
			timestamp = meta_data[1]
			start = int(meta_data[2])
			stop = start + window_size
			lines = lines[1:]

		for line in lines:
			value = map(stripAndConvertToFloat, line.split(', '))
			values.append(value)

	y_values = [y for [x,y,z] in values]
	y_values_smooth = savitzky_golay(np.array(y_values), 11, 3)
	detected_start = detect_start(y_values_smooth)

	if detected_start != -1:
		start = detected_start
		stop = start + window_size
	else:
		print "couldn't detect start in: " + file_path

	return values[start:stop]

def stripAndConvertToFloat(str):
	return float(str.strip())

def detect_start(data):
	data = np.array(data)
	maxima = argrelextrema(data = data, comparator = np.greater_equal, order = 3)
	minima = argrelextrema(data = data, comparator = np.less_equal, order = 3)
	extremum_list = create_extremum_list(maxima, minima)

	global_maximum = np.amax(data)
	global_minimum = np.amin(data)
	#upper_thresh = 0.6 * global_maximum
	#lower_thresh = 0.6 * global_minimum
	
	for i in range(len(extremum_list)-1):
		current_tuple = extremum_list[i]
		current_position = current_tuple[0]
		current_type = current_tuple[1]

		if current_type == 0:
			continue

		current_value = data[current_position]

		#if current_value < lower_thresh:

		next_tuple = extremum_list[i+1]
		next_position = next_tuple[0]
		next_type = next_tuple[1]
		next_value = data[next_position]

		#if next_type == 0 and next_value > upper_thresh:
		if next_type == 0 and abs(next_value-current_value) > 0.7 * (global_maximum-global_minimum):
			return current_position

	return -1

def create_extremum_list(maxima_list, minima_list):

	zipped_list = []

	for i in range(len(maxima_list[0])):
		zipped_list.append((maxima_list[0].item(i), 0))

	for i in range(len(minima_list[0])):
		zipped_list.append((minima_list[0].item(i), 1))

	zipped_list = sorted(zipped_list, key=lambda tup: tup[0])
	return zipped_list

def savitzky_golay(y, window_size, order, deriv=0, rate=1):
	try:
		window_size = np.abs(np.int(window_size))
		order = np.abs(np.int(order))
	except ValueError, msg:
		raise ValueError("window_size and order have to be of type int")
	if window_size % 2 != 1 or window_size < 1:
		raise TypeError("window_size size must be a positive odd number")
	if window_size < order + 2:
		raise TypeError("window_size is too small for the polynomials order")
	order_range = range(order+1)
	half_window = (window_size -1) // 2
	# precompute coefficients
	b = np.mat([[k**i for i in order_range] for k in range(-half_window, half_window+1)])
	m = np.linalg.pinv(b).A[deriv] * rate**deriv * factorial(deriv)
	# pad the signal at the extremes with
	# values taken from the signal itself
	firstvals = y[0] - np.abs( y[1:half_window+1][::-1] - y[0] )
	lastvals = y[-1] + np.abs(y[-half_window-1:-1][::-1] - y[-1])
	y = np.concatenate((firstvals, y, lastvals))
	return np.convolve( m[::-1], y, mode='valid')


if __name__ == '__main__':
	file = sys.argv[1]
	if isfile(file):
		plot(file)
	if isdir(file):
		plot_all_files_in_dir(file)
	else:
		print "Wrong input."