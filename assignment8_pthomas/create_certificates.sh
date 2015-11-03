#!/bin/bash
#
# USAGE : 
#	bash create_certificates.sh <user-name>
#

CERT_NAME=$1
CERT_DIRNAME=certificates
COMPANY_NAME=mycompany
COMPANY_SITE_ARG="CN=www.${COMPANY_NAME}.com"
EMAIL_ADDR_ARG="emailAddress=${CERT_NAME}@${COMPANY_NAME}.com"
SUBJ_STRING="/C=US/O=${COMPANY_NAME}/OU=IT/${COMPANY_SITE_ARG}/${EMAIL_ADDR_ARG}"

if [ ! -d $CERT_DIRNAME ]; then
	mkdir $CERT_DIRNAME
fi

rm -f ${CERT_DIRNAME}/${CERT_NAME}.*

echo "Creating ${CERT_DIRNAME}/${CERT_NAME}.pem"
openssl req -new -nodes -newkey rsa:2048 -x509 \
	-keyout ${CERT_DIRNAME}/${CERT_NAME}.key -days 3650 \
	-out ${CERT_DIRNAME}/${CERT_NAME}.pem  \
	-subj ${SUBJ_STRING}
