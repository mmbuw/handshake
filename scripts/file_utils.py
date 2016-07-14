import sys
import itertools
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from os import listdir
from os.path import isfile, join
import matplotlib.pyplot as plt
import math

def load_values_from_file(file_path, window_size):
	values = []
	with open(file_path, 'r') as f:
		lines = f.readlines()

		if lines[0].startswith('#'):
			meta_data = lines[0].split(', ')
			devicename = meta_data[0]
			timestamp = meta_data[1]
			start = int(meta_data[4])
			stop = start + window_size
			lines = lines[start+1:stop+1]

		for line in lines:
			value = map(stripAndConvertToFloat, line.split(', '))
			values.append(value)

	return values

def stripAndConvertToFloat(str):
	return float(str.strip())