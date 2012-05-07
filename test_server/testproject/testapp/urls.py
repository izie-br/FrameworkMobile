from django.conf.urls import patterns, include, url

urlpatterns = patterns('testproject.testapp.views',
    (r'^documents/get_json/','get_json_objects',{'classname':'Document'}),
    (r'^authors/get_json/','get_json_objects',{'classname':'Author'}),
    (r'^authors/echo_json/','echo_json_objects',{'classname':'Author'}),
    (r'^authors/save_json/','save_json_objects',{'classname':'Author'}),
    (r'^authors/clear/','clear_objects',{'classname':'Author'}),
)
