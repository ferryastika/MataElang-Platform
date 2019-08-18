FROM alpine:latest
ARG PROTECTED_SUBNET
ARG EXTERNAL_SUBNET
ARG OINKCODE

# Instalasi package yang diperlukan melalui apt
COPY require /root

RUN apk update && apk add --no-cache \
	alpine-sdk \
	linux-headers \
	libpcap-dev \
	libdnet-dev \
	musl-dev \
	pcre-dev \
	bison \
	flex \
	daq-dev \
	net-tools \
	wget \
	zlib-dev \
	supervisor \
	python3-dev \
	sed \
	tar \
	libtirpc-dev \
	libressl-dev \
	cmake \
	make \
	g++ \
	busybox \
	shadow \
	perl-net-ssleay \
	perl-lwp-useragent-determined \
	perl-lwp-protocol-https \
	perl-libwww \
	perl-crypt-ssleay &&\

	ln -s /usr/include/tirpc/rpc /usr/include/rpc &&\
	ln -s /usr/include/tirpc/netconfig.h /usr/include/netconfig.h &&\
# File requirement yang dibutuhkan package python untuk menjalankan snort

	pip3 install --no-cache-dir --upgrade pip setuptools wheel && \
	hash -r pip && \
	pip3 install --no-cache-dir -r /root/requirements.txt &&\

# Membuat direktori untuk source file daq, snort dan pulledpork

	mkdir -p /root/snort_src && \
	mkdir -p /root/pulledpork_src &&\
	cd /root &&\

# Download source snort dan simpan pada directory ~/daq_src

	wget https://www.snort.org/downloads/snort/snort-2.9.11.1.tar.gz -O snort.tar.gz &&\
	tar -xvzf snort.tar.gz --strip-components=1 -C /root/snort_src &&\
	rm snort.tar.gz &&\

# Download source pulledpork dan simpan pada directory ~/daq_src

	wget https://github.com/shirkdog/pulledpork/archive/v0.7.3.tar.gz -O pulledpork.tar.gz &&\
	tar -xvzf pulledpork.tar.gz --strip-components=1 -C /root/pulledpork_src &&\
	rm pulledpork.tar.gz &&\

# Compile source code dari snort selanjutnya

	cd /root/snort_src && \
	./configure --enable-sourcefire && \
	make && \
	make install && \
	ln -s /usr/local/bin/snort /usr/sbin/snort &&\

# Tambahkan username, user dan group dengan nama "snort"

	addgroup -S snort && \
	adduser -S snort -g snort &&\

# Buat direktori kerja snort

	mkdir /etc/snort && \
	mkdir /etc/snort/rules && \
	mkdir /etc/snort/rules/iplists && \
	mkdir /etc/snort/preproc_rules && \
	mkdir /etc/snort/so_rules && \
	mkdir /var/log/snort && \
	mkdir /var/log/snort/archived_logs && \
	mkdir /usr/local/lib/snort_dynamicrules && \
	mkdir /tmp/snort && \
	touch /etc/snort/rules/iplists/white_list.rules /etc/snort/rules/iplists/black_list.rules /etc/snort/rules/local.rules /etc/snort/sid-msg.map && \
	chmod -R 5775 /etc/snort && \
	chmod -R 5775 /var/log/snort && \
	chmod -R 5775 /var/log/snort/archived_logs && \
	chmod -R 5775 /usr/local/lib/snort_dynamicrules && \
	chown -R snort:snort /etc/snort && \
	chown -R snort:snort /var/log/snort && \
	chown -R snort:snort /usr/local/lib/snort_dynamicrules && \
	cp /root/snort_src/etc/*.conf* /etc/snort && \
	cp /root/snort_src/etc/*.map /etc/snort && \
	cp /root/snort_src/etc/*.dtd /etc/snort && \
	cp /root/snort_src/src/dynamic-preprocessors/build/usr/local/lib/snort_dynamicpreprocessor/* /usr/local/lib/snort_dynamicpreprocessor/ &&\

# Lakukan edit konfigurasi yang dibutuhkan pada file /etc/snort/snort.conf

	sed -i \
	-e 's@^ipvar HOME_NET.*@ipvar HOME_NET '"${PROTECTED_SUBNET}"'@' \
	-e 's@^ipvar EXTERNAL_NET.*@ipvar EXTERNAL_NET '"${EXTERNAL_SUBNET}"'@' \
	-e 's@^var RULE_PATH.*@var RULE_PATH /etc/snort/rules@' \
	-e 's@^var SO_RULE_PATH.*@var SO_RULE_PATH /etc/snort/so_rules@' \
	-e 's@^var PREPROC_RULE_PATH.*@var PREPROCRULE_PATH /etc/snort/preproc_rules@' \
	-e 's@^var WHITE_LIST_PATH.*@var WHITE_LIST_PATH /etc/snort/rules/iplists@' \
	-e 's@^var BLACK_LIST_PATH.*@var BLACK_LIST_PATH /etc/snort/rules/iplists@' \
	-e 's@^\(include $.*\)@# \1@' \
	-e 's@\# include \$RULE\_PATH\/local\.rules@include \$RULE\_PATH\/local\.rules@' \
	-e '/include \$RULE\_PATH\/local\.rules/a include \$RULE\_PATH\/snort\.rules' \
		/etc/snort/snort.conf &&\

# Instalasi source pulledpork

	cp /root/pulledpork_src/pulledpork.pl /usr/local/bin && \
	chmod +x /usr/local/bin/pulledpork.pl &&\
	cp /root/pulledpork_src/etc/*.conf /etc/snort &&\
	cp /root/pulledpork.conf /etc/snort &&\

	sed -i 's@.oinkcode.@'"${OINKCODE}"'@' /etc/snort/pulledpork.conf &&\

	/usr/local/bin/pulledpork.pl -c /etc/snort/pulledpork.conf -l &&\
	snort -T -c /etc/snort/snort.conf &&\

	sed -i '/import alert/c\import snortunsock.alert as alert' /usr/lib/python3.6/site-packages/snortunsock/snort_listener.py &&\

# Cleanup
	rm -rf /root/snort_src /root/pulledpork_src /root/requirements.txt /root/pulledpork.conf &&\
	apk del net-tools wget

EXPOSE 5000

ENTRYPOINT ["/usr/bin/supervisord","-c","/root/libs/super_snort.conf"]
