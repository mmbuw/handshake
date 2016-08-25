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
	xy = [ magnitude([a[0], a[1]]) for a in values]
	xz = [ magnitude([a[0], a[2]]) for a in values]
	yz = [ magnitude([a[1], a[2]]) for a in values]
	mag = [magnitude(a) for a in values]

	data = {}

	data["x"] = x
	data["y"] = y
	data["z"] = z
	data["xyz"] = mag

	fft_x = fft_utils.get_fft_vector(x)
	fft_y = fft_utils.get_fft_vector(y)
	fft_z = fft_utils.get_fft_vector(z)
	fft_mag = fft_utils.get_fft_vector(mag)

	data["fft_x"] = fft_x.tolist()
	data["fft_y"] = fft_y.tolist()
	data["fft_z"] = fft_z.tolist()
	data["fft_xyz"] = fft_mag.tolist()

	fft_xy = fft_utils.get_fft_vector(xy)
	fft_xz = fft_utils.get_fft_vector(xz)
	fft_yz = fft_utils.get_fft_vector(yz)

	data["fft_xy"] = fft_xy.tolist()
	data["fft_xz"] = fft_xz.tolist()
	data["fft_yz"] = fft_yz.tolist()

	mean_x = np.mean(x)
	mean_y = np.mean(y)
	mean_z = np.mean(z)

	data["mean_x"] = [mean_x]
	data["mean_y"] = [mean_y]
	data["mean_z"] = [mean_z]

	#range_x = np.ptp(x)
	#range_y = np.ptp(y)
	#range_z = np.ptp(z)

	#data["range_x"] = [range_x]
	#data["range_y"] = [range_y]
	#data["range_z"] = [range_z]

	crossings_x = len( np.where( np.diff( np.sign( x ) ) )[0] )
	crossings_y = len( np.where( np.diff( np.sign( y ) ) )[0] )
	crossings_z = len( np.where( np.diff( np.sign( z ) ) )[0] )

	data["crossings_x"] = [crossings_x]
	data["crossings_y"] = [crossings_y]
	data["crossings_z"] = [crossings_z]

	return data

def multiply_elementwise(lista, listb):
	return [a*b for a,b in zip(lista,listb)]

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

	with open('shake_data_mag.json', 'w') as outfile:
		json.dump(data, outfile)