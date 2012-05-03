from django.conf.urls import patterns, include, url

urlpatterns = patterns('testproject.testapp.views',
    (r'^documents/get_json/','get_documents_json'),
    (r'^authors/get_json/','get_authors_json'),
)
