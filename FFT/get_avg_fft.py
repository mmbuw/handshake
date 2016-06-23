import sys
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial
import pickle

def get_fft_avg_from_dir(file_dir):
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	fft_avg = [0] * 200
	for file in txtfiles:
		fft_avg = [x + y for x, y in zip(fft_avg, get_fft(file))]
	fft_avg = [ x / len(txtfiles) for x in fft_avg]

	#with open('fft_avg.pickle', 'w') as f:
	pickle.dump(fft_avg, open('fft_avg.pickle', 'w'))

	#plt.plot(fft_avg)
	#plt.show()


def get_fft(file):
	values = []

	with open(file, 'r') as f:
		lines = f.readlines()

		if lines[0].startswith('#'):
			meta_data = lines[0].split(', ')
			devicename = meta_data[0]
			timestamp = meta_data[1]
			meta_start = int(meta_data[2]) + 1
			meta_stop = int(meta_data[3]) + 1
			lines = lines[1:]

		for line in lines:
			value = line.split(', ')[1]
			values.append(float(value))

	if len(sys.argv) > 2:
		start = int(sys.argv[2])
		stop = int(sys.argv[3])
	else:
		start = meta_start
		stop = meta_stop


	fft_values_complex = fft(values)
	fft_values = abs(fft(fft_values_complex)).real
	fft_values = fft_values[:len(fft_values)/2]
	fft_values_cut = abs(fft(values[start:stop])).real
	fft_values_cut = fft_values_cut[:len(fft_values_cut)/2]

	return fft_values_cut

def get_title_from_path(path):
	return path.split('/')[-1].split('.')[0]

if __name__ == '__main__':
	file = sys.argv[1]
	get_fft_avg_from_dir(file)