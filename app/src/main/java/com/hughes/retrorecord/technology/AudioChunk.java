package com.hughes.retrorecord.technology;

public class AudioChunk {
	short[] data;
	int length = 0;
	int used = 0;
	
	public AudioChunk(int size) {
		data = new short[size];
	}
}
