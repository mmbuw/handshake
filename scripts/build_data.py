import fft_utils
import file_utils
import math
import os
import sys
from os import listdir
from os.path import isfile, join
import itertools
import numpy as np
import json

def get_values(txtfile, window_size):
	values = [a for a in file_utils.load_values_from_file(txtfile, window_size)]
	x = [a[0] for a in values]
	y = [a[1] for a in values]
	z = [a[2] for a in values]
	mag = [magnitude(a) for a in values]

	data = {}

	data["x"] = x
	data["y"] = y
	data["z"] = z
	data["magnitude"] = mag

	fft_x = fft_utils.get_fft_vector(x)
	fft_y = fft_utils.get_fft_vector(y)
	fft_z = fft_utils.get_fft_vector(z)
	fft_mag = fft_utils.get_fft_vector(mag)

	data["fft_x"] = fft_x.tolist()
	data["fft_y"] = fft_y.tolist()
	data["fft_z"] = fft_z.tolist()
	data["fft_magnitude"] = fft_mag.tolist()

	return data

def magnitude(vector):
	magnitude = 0.0
	for entry in vector:
		magnitude += entry * entry
	return math.sqrt(magnitude)

def get_data(window_size):
	data = {}
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	for txtfile in txtfiles:
		name = txtfile.split("/")[-1]
		data[name] = get_values(txtfile, window_size)
	return data

if __name__ == '__main__':
	data = {}
	for x in range(5, 105, 5):
		data[x] = get_data(x)

	with open('data.json', 'w') as outfile:
		json.dump(data, outfile)