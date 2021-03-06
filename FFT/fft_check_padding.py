import sys
import itertools
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from os import listdir
from os.path import isfile, join
import matplotlib.pyplot as plt
import math

#
#
# Calculates the fft similarities between signals represented by text files in a folder
# usage: python(2.7) fft.py <path-to-dir>

def simCos(v1, v2):
	return 1 - spatial.distance.cosine(v1, v2)

def get_fft_vector(vector, window_size, minF=2, maxF=15):
	vector += [0] * (window_size - len(vector)) #padding with zeroes
	vector = fft(vector)
	vector = vector[1:0.07*len(vector)]
	for u in range(len(vector)):
		vector[u] = abs(vector[u])
	return vector.real

def load_values_from_file(file_path):
	values = []
	with open(file_path, 'r') as f:
		lines = f.readlines()

		if lines[0].startswith('#'):
			meta_data = lines[0].split(', ')
			devicename = meta_data[0]
			timestamp = meta_data[1]
			start = int(meta_data[2])
			stop = int(meta_data[3])
			lines = lines[start+1:stop]

		for line in lines:
			value = line.split(', ')[1]
			values.append(value)
	return values

def evaluate(file1, file2, window_size):
	values1 = load_values_from_file(file1)
	values2 = load_values_from_file(file2)
	vector1 = get_fft_vector(values1, window_size)
	vector2 = get_fft_vector(values2, window_size)
	simi = simCos(vector1, vector2) * 100
	result = (simi, vector1, vector2)

	return result

def get_fft_difference(file1, file2, window_size):
	values1 = load_values_from_file(file1)
	values2 = load_values_from_file(file2)
	vector1 = get_fft_vector(values1, window_size)
	vector2 = get_fft_vector(values2, window_size)

	return vector1 - vector2

def get_title_from_path(path):
	return path.split('/')[-1].split('.')[0]

def get_plot_title(result):
	title1 = get_title_from_path(result[0][0])
	title2 = get_title_from_path(result[0][1])
	return title1 + ' - ' +title2

def main():
	window_size = 200
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	results = []
	for pair in txtfilepairs:
		results.append((pair, evaluate(pair[0], pair[1], window_size)))
	
	results.sort()

	plot_size = len(results)
	fig = plt.figure()
	title = ''
	for i, result in enumerate(results):
		plt.subplot(plot_size/3, 3, i)
		plt.plot(result[1][1].real)
		plt.plot(result[1][2].real)
		#sim = str(result[1][0])[1:7]
		sim = str(result[1][0])
		plt.title(get_plot_title(result)+"\nsim: "+sim)
	fig.set_size_inches(16, 10)
	plt.tight_layout()
	plt.savefig(file_dir+'/fft_padded-'+str(window_size)+'.png', dpi = 300)
	#plt.show()


if __name__ == '__main__':
	main()