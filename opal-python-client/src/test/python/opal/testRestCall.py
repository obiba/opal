from argparse import Namespace
import pycurl
import unittest
from opal.core import OpalClient


class OpalClientTestSSLConnection(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        setattr(cls, 'PORT', '8080')
        setattr(cls, 'SERVER', 'http://localhost')
        setattr(cls, 'SSL_PORT', '8443')
        setattr(cls, 'SSL_SERVER', 'https://localhost')
        # Make sure to place your own certificate files
        setattr(cls, 'SSL_CERTIFICATE', '../../resources/certificates/publickey.pem')
        setattr(cls, 'SSL_KEY', '../../resources/certificates/privatekey.pem')

    def test_sendRestBadServer(self):
        client = OpalClient.buildWithAuthentication(server='http://deadbeef:8080', user='administrator',
                                                    password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRestBadCredentials(self):
        client = OpalClient.buildWithAuthentication(server="%s:%s" % (self.SERVER, self.PORT), user='admin',
                                                    password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRest(self):
        try:
            client = OpalClient.buildWithAuthentication(server="%s:%s" % (self.SERVER, self.PORT), user='administrator',
                                                        password='password')
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_sendSecuredRest(self):
        try:
            client = OpalClient.buildWithCertificate(server="%s:%s" % (self.SSL_SERVER, self.SSL_PORT),
                                                     cert=self.SSL_CERTIFICATE,
                                                     key=self.SSL_KEY)
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_validAuthLoginInfo(self):
        try:
            args = Namespace(opal="%s:%s" % (self.SERVER, self.PORT), user='administrator', password='password')
            client = OpalClient.build(loginInfo=OpalClient.LoginInfo.parse(args))
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_validSslLoginInfo(self):
        try:
            args = Namespace(opal="%s:%s" % (self.SSL_SERVER, self.SSL_PORT), ssl_cert=self.SSL_CERTIFICATE,
                             ssl_key=self.SSL_KEY)
            client = OpalClient.build(loginInfo=OpalClient.LoginInfo.parse(args))
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_invalidServerInfo(self):
        args = Namespace(opl="%s:%s" % (self.SERVER, self.PORT), user='administrator', password='password')
        self.assertRaises(Exception, OpalClient.LoginInfo.parse, args);

    def test_invalidLoginInfo(self):
        args = Namespace(opal="%s:%s" % (self.SERVER, self.PORT), usr='administrator', password='password')
        self.assertRaises(Exception, OpalClient.LoginInfo.parse, args);

    def __sendSimpleRequest(self, request):
        request.fail_on_error()
        request.accept_json()
        # uncomment for debugging
        # request.verbose()

        # send request
        request.method('GET').resource('/datasource/ONYX/table/DatabaseTest/variables')
        response = request.send()

        # format response
        res = response.content

        # output to stdout
        print res

