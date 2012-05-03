from django.contrib import admin
from testproject.testapp.models import Document, Author

class GenericAdmin(admin.ModelAdmin):
    pass

admin.site.register(Document, GenericAdmin)
admin.site.register(Author, GenericAdmin)
