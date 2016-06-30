import sys
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial
from numpy import linalg as la

def plot_all_files_in_dir(file_dir):
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	for file in txtfiles:
		plot(file, show_plot=False)

def add_new_start_for_all(file_dir):
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	txtfiles.sort()
	for file in txtfiles:
		print get_title_from_path(file)
		new_start = input('enter new start: ')
		add_new_start(file, new_start)
		print

def add_new_start(file, new_start):
	with open(file, 'r+') as f:
		lines = f.readlines()
		lines[0] = lines[0].strip() + ', ' + str(new_start) + '\n'
		f.seek(0)
		for line in lines:
			f.write(line)

def plot(file, show_plot=True):
	values = []

	with open(file, 'r') as f:
		lines = f.readlines()

		if lines[0].startswith('#'):
			meta_data = lines[0].split(', ')
			devicename = meta_data[0]
			timestamp = meta_data[1]
			meta_start = int(meta_data[2]) #+ 1
			meta_stop = int(meta_data[3]) #+ 1
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


	stop = start + 70

	fft_values = fft(values[start:stop])
	fft_values = abs(fft_values).real
	fft_values = fft_values[:len(fft_values)/2]

	title = get_title_from_path(file)

	fig_title = "%s start: %s stop: %s" % (title, start, stop)
	fig = plt.figure(figsize=(16,9))
	fig.suptitle(fig_title)

	plt.title(title)

	plt.subplot(3,1,1)
	plt.axvline(start, color="red")
	plt.grid(True)
	plt.plot(values)
	plt.ylim([-25,25])
	plt.xlim([start-35,start+35])
	plt.xticks(np.arange(start-35, start+35, 2.0))
	
	plt.subplot(3,1,2)
	plt.axvline(start, color="red")
	plt.axvline(stop, color="red")
	plt.grid(True)
	plt.plot(values)
	plt.ylim([-25,25])
	plt.xlim([0,300])
	
	plt.subplot(3,1,3)
	plt.grid(True)
	plt.ylim([0,1000])
	plt.xlim([0,40])
	plt.plot(fft_values)

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
	if isfile(file):
		plot(file)
	if isdir(file):
		add_new_start_for_all(file)
		#plot_all_files_in_dir(file)
	else:
		print "Wrong input."
