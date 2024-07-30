import requests

url = "http://localhost:8080/optimizationRun/1/chromosome/notEvaluated"

response = requests.request("GET", url, data=payload)

print(response.text)