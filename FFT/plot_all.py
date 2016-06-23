import sys
from scipy.fftpack import fft, rfft, ifft
from os.path import isfile, join
from os import listdir
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial

def main():

	file_dir = sys.argv[1]
	txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]

	fig = plt.figure()

	plt.subplot(2,1,1)
	plt.grid(True)
	plt.ylim([-30,30])
	plt.xlim([0,250])
	
	plt.subplot(2,1,2)
	plt.grid(True)
	plt.ylim([0,5000])
	plt.xlim([0,125])

	for file in txtfiles:
		values = []
		with open(file, 'r') as f:
			lines = f.readlines()

			if lines[0].startswith('#'):
				meta_data = lines[0].split(', ')
				devicename = meta_data[0]
				timestamp = meta_data[1]
				start = int(meta_data[2]) + 1
				stop = int(meta_data[3]) + 1
				lines = lines[1:]

			for line in lines:
				value = line.split(', ')[1]
				values.append(float(value))
		fft_values_complex = fft(values[start:stop])
		fft_values = abs(fft(fft_values_complex)).real
		fft_values = fft_values[:len(fft_values)/2]
		plt.subplot(2,1,1)
		plt.plot(values)
		plt.subplot(2,1,2)
		plt.plot(fft_values)



	
	plt.tight_layout()
	plt.show()

if __name__ == '__main__':
	main()