import sys
from scipy.fftpack import fft, rfft, ifft
import numpy
from os import listdir
from os.path import isfile, join
import matplotlib.pyplot as plt
from collections import Counter

# usage: python(2.7) fft_peak.py <path-to-dir>

def load_values_from_file(file_path):
	values = []
	with open(file_path, 'r') as f:
		for line in f.readlines():
			value = line.split(', ')[1]
			values.append(value)
	return values

def main():
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]

	window_size = 1000

	counter_list = []
	vector_size_list = []

	for file in txtfiles:
		values = load_values_from_file(file)
		vector_size_list.append(len(values))
		values += [0] * (window_size - len(values))
		vector = abs(fft(values)).tolist()
		print len(vector)
		max_freq_idx = vector.index(max(vector))
		print max_freq_idx
		counter_list.append(max_freq_idx)

	counter = Counter(counter_list)
	print(counter)

	print(numpy.mean(vector_size_list))

	labels, values = zip(*counter.items())

	indexes = numpy.arange(len(labels))
	width = 1

	plt.bar(indexes, values, width)
	plt.xticks(indexes + width * 0.5, labels)
	plt.show()


if __name__ == '__main__':
	main()