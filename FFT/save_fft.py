import sys
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import pickle
import numpy as np

def load_values_from_file(file):
	values = []
	with open(file, 'r') as f:
		lines = f.readlines()
		if lines[0].startswith('#'):
			lines = lines[1:]
		for line in lines:
			value = line.split(', ')[1]
			values.append(float(value))
	return values

def get_fft_magnitude(values):
	fft_values_complex = fft(values)
	fft_values = abs(fft(fft_values_complex)).real
	fft_values = fft_values[:len(fft_values)/2]
	return fft_values

def save_values(values, outfile):
	pickle.dump(fft_values, open(outfile, 'w'))



if __name__ == '__main__':
	infile = sys.argv[1]
	outfile = sys.argv[2]
	start = int(sys.argv[3])
	stop = int(sys.argv[4])
	values = load_values_from_file(infile)
	values = values[start:stop]
	fft_values = get_fft_magnitude(values)
	save_values(fft_values, outfile)

	