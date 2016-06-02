import sys
import itertools
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from os import listdir
from os.path import isfile, join
import matplotlib.pyplot as plt

#
#
# Calculates the fft similarities between signals represented by text files in a folder
# usage: python(2.7) fft.py <path-to-dir>

def sim(v1, v2):
	return 1 - spatial.distance.cosine(v1, v2)

def compare_vector_lists(vlist1, vlist2):
	result = []
	for idx1, v1 in enumerate(vlist1):
		for idx2, v2 in enumerate(vlist2):
			simi = sim(v1, v2)
			result.append((idx1, idx2, simi))
	return result

def get_fft_vector_list(values, window_size, step_size):
	i = 0
	j = window_size
	vectors = []
	while j < len(values):
		window = values[i:j]
		vector = fft(window)
		vector = vector[2:35]
		#vector = vector[1:len(vector)/2]
		for u in range(len(vector)):
			vector[u] = abs(vector[u])
		vectors.append(vector)
		i += step_size
		j += step_size
	return vectors


def load_values_from_file(file_path):
	values = []
	with open(file_path, 'r') as f:
		for line in f.readlines():
			value = line.split(', ')[1]
			values.append(value)
	return values

def evaluate(file1, file2):
	values1 = load_values_from_file(file1)
	values2 = load_values_from_file(file2)
	window_size = min(len(values1), len(values2)) - 1
	step_size = 2
	vlist1 = get_fft_vector_list(values1, window_size, step_size)
	vlist2 = get_fft_vector_list(values2, window_size, step_size)
	results = compare_vector_lists(vlist1, vlist2)
	
	max_sim = 0
	#print file1 + " - " + file2
	max_result = ()
	for idx, result in enumerate(results):
		sim = result[2]*100
		if sim > max_sim:
			max_sim = sim
			idx1 = result[0]
			idx2 = result[1]
			max_result = (sim, vlist1[idx1], vlist2[idx2])
	#	print str(result[0]) +" : "+ str(result[1]) +"\t"+ str(sim)
	return max_result

def get_title_from_path(path):
	return path.split('/')[-1].split('.')[0]

def get_plot_title(result):
	title1 = get_title_from_path(result[0][0])
	title2 = get_title_from_path(result[0][1])
	return title1 + ' - ' +title2

def main():
	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	results = []
	for pair in txtfilepairs:
		results.append((pair, evaluate(pair[0], pair[1])))
	
	results.sort()

	plot_size = len(results)
	fig = plt.figure()
	title = ''
	for i, result in enumerate(results):
		plt.subplot(plot_size/3, 3, i)
		plt.plot(result[1][1].real)
		plt.plot(result[1][2].real)
		sim = str(result[1][0])[1:7]
		plt.title(get_plot_title(result)+"\nsim: "+sim)
	fig.set_size_inches(16, 10)
	plt.tight_layout()
	plt.savefig(file_dir+'/fft.png', dpi = 300)
	#plt.show()


if __name__ == '__main__':
	main()