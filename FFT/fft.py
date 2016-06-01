import sys
import itertools
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from os import listdir
from os.path import isfile, join

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
		# print(vector)
		# sys.exit(0)
		vector = vector[:len(vector)/2]
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
	for result in results:
		sim = result[2]*100
		if sim > max_sim:
			max_sim = sim
	#	print str(result[0]) +" : "+ str(result[1]) +"\t"+ str(sim)
	return max_sim

def main():
	file_dir = sys.argv[1]
	txtfiles = [f for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfilepairs = list(itertools.combinations(txtfiles, 2))
	results = []
	for pair in txtfilepairs:
		results.append((pair, evaluate(pair[0], pair[1])))
	
	results.sort()

	for result in results:
		print result[0][0] +" - "+ result[0][1] +":\t"+ str(abs(result[1]))
		if result[0][0][:6] == result[0][1][:6]:
			print "---------------------------"



if __name__ == '__main__':
	main()