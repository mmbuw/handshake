import sys
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial

def plot_all_files_in_dir(file_dir):
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	for file in txtfiles:
		plot(file, show_plot= False)


def plot(file, show_plot=True):
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

	fig_title = "%s start: %s stop: %s" % (title, start, stop)
	fig = plt.figure()
	fig.suptitle(fig_title)

	plt.title(title)

	plt.subplot(3,1,1)
	plt.grid(True)
	plt.axvline(start, color="red")
	plt.axvline(stop, color="red")
	plt.plot(values)
	plt.ylim([-30,30])
	plt.xlim([0,200])
	
	plt.subplot(3,1,2)
	plt.grid(True)
	plt.plot(fft_values)
	plt.ylim([0,2000])
	plt.xlim([0,100])
	
	plt.subplot(3,1,3)
	plt.grid(True)
	plt.ylim([0,1000])
	plt.xlim([0,40])
	plt.plot(fft_values_cut)
	
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
		plot_all_files_in_dir(file)
	else:
		print "Wrong input."
