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

    def test_sendRestBadServer(self):
        client = OpalClient.build(server='http://badserver:8080', user='administrator',
                                  password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRestBadCredentials(self):
        client = OpalClient.build(server="%s:%s" % (self.SERVER, self.PORT), user='admin', password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRest(self):
        try:
            client = OpalClient.build(server="%s:%s" % (self.SERVER, self.PORT), user='administrator',
                                      password='password')
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_sendSecuredRest(self):
        try:
            client = OpalClient.buildSecured(server="%s:%s" % (self.SSL_SERVER, self.SSL_PORT),
                                             cert='/home/rhaeri/mica/mica-publickey.pem',
                                             key='/home/rhaeri/mica/mica-privatekey.pem')
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

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



