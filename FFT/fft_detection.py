import sys
from scipy.fftpack import fft, rfft, ifft
from scipy import spatial
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
import pickle
from scipy import spatial
from numpy import linalg as la

def plot_all_files_in_dir(file_dir):

	#with open('fft_avg.pickle', 'r') as f:
	#fft_avg = pickle.load(open('perfect_shake.pickle', 'r'))

	fft_avg = [0.5, 0.2, 3, 0.1]
	fft_avg = fft_avg / la.norm(fft_avg)

	thres = 0.7

	print fft_avg

	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	for file in txtfiles:
		values = load_values_from_file(file)
		(start_max, stop_max, sim_max) = detect2(values, fft_avg)
		try:
			(start_first, stop_first, sim_first) = detect2_first(values, fft_avg, thres)
			plot(file, start_max, stop_max, sim_max, start_first, stop_first, sim_first, fft_avg, show_plot=False)
		except Exception, e:
			print e

def simCos(v1, v2):
	return 1 - spatial.distance.cosine(v1, v2)

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

def detect2(values, fft_avg):
	window_size = 80
	peak = 0
	maxSim = 0
	for i in range(0,len(values)-window_size):
		window = values[i:i+window_size]
		fft_values = get_fft_magnitude(window)
		fft_values = fft_values / la.norm(fft_values)
		fft_values = fft_values[:len(fft_avg)]
		sim = simCos(fft_values, fft_avg)
		if sim > maxSim:
			maxSim = sim
			peak = i
	return (peak, peak + window_size, maxSim)

def detect(values, fft_avg):
	window_size = len(fft_avg)*2
	peak = 0
	maxSim = 0
	for i in range(0,len(values)-window_size):
		window = values[i:i+window_size]
		fft_values = get_fft_magnitude(window)
		sim = simCos(fft_avg, fft_values)
		if sim > maxSim:
			maxSim = sim
			peak = i
	return (peak, peak + window_size, maxSim)

def detect2_first(values, fft_avg, thres):
	window_size = 80
	for i in range(0,len(values)-window_size):
		window = values[i:i+window_size]
		fft_values = get_fft_magnitude(window)
		fft_values = fft_values / la.norm(fft_values)
		fft_values = fft_values[:len(fft_avg)]
		sim = simCos(fft_values, fft_avg)
		if sim > thres:
			return (i, i + window_size, sim)

def get_fft_magnitude(values):
	fft_values_complex = fft(values)
	fft_values = abs(fft(fft_values_complex)).real
	fft_values = fft_values[:len(fft_values)/2]
	return fft_values


def plot(file, start_max, stop_max, sim_max, start_first, stop_first, sim_first, fft_avg, show_plot=True):
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

	title = get_title_from_path(file)

	fig_title = "%s start: %s stop: %s sim_max: %s sim_first: %s" \
		% (title, start, stop, round(sim_max, 2), round(sim_first, 2))

	fig = plt.figure()
	fig.suptitle(fig_title)

	plt.title(title)

	plt.subplot(4,1,1)
	plt.grid(True)
	plt.axvline(start, color="red")
	plt.axvline(start_max, color="green")
	plt.axvline(start_first, color="purple")
	plt.axvline(stop, color="red")
	plt.axvline(stop_max, color="green")
	plt.axvline(stop_first, color="purple")
	plt.plot(values)
	plt.ylim([-30,30])
	plt.xlim([0,400])
	
	plt.subplot(4,1,2)
	plt.grid(True)
	plt.plot(fft_values)
	plt.ylim([0,2000])
	plt.xlim([0,200])
	
	plt.subplot(4,1,3)
	plt.grid(True)
	plt.ylim([0,1000])
	plt.xlim([0,80])
	plt.plot(fft_values_cut)

	plt.subplot(4,1,4)
	plt.grid(True)
	plt.ylim([0,1])
	plt.xlim([0,80])
	plt.plot(fft_avg)
	
	plt.tight_layout()

	if show_plot:
		plt.show()

	save_file = splitext(file)[0]+".png"
	plt.savefig(save_file, dpi = 160)
	plt.close()

def get_title_from_path(path):
	return path.split('/')[-1].split('.')[0]

if __name__ == '__main__':
	file = sys.argv[1]
	plot_all_files_in_dir(file)