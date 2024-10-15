import subprocess
import json
import time
import openapi_client
from openapi_client import ObjectiveFunctionApi
from openapi_client import OptimizationRunApi
from openapi_client import ObjectiveFunctionResponseBody
from openapi_client import CreateOptimizationRunResponseBody


collectedData = dict()
collectedData["bestFitness"] = list()
collectedData["bestChromosomeElements"] = list()

api_client = openapi_client.ApiClient()
objectiveFunctionApi = ObjectiveFunctionApi(api_client)
optimizationRunApi = OptimizationRunApi(api_client)

numeroContainers = [1, 2, 3, 4, 5]
rodadasPorNumeroDeContainers = 2

subprocess.call("docker stack deploy -c docker-compose.yml app", shell=True)

def createObjectiveFunction()->ObjectiveFunctionResponseBody:
    with open("createObjectiveFunctionRequestBody.json","r") as file:
        fileData = json.load(file)
        return objectiveFunctionApi.create_objective_function(fileData)

createObjectiveFunctionResponse = createObjectiveFunction()
objectiveFunctionId = createObjectiveFunctionResponse.id

def createOptimizationRun()->CreateOptimizationRunResponseBody:
    with open("createOptimizationRunRequestBody.json", 'r') as file:
        fileData = json.load(file)
        fileData["objectiveFunctionId"] = objectiveFunctionId
        return optimizationRunApi.create_optimization_run(optimization_run_request_body=fileData)

def changeOptimizationRunIdFile(newOptimizationRunId: int): 
    with open("src/main/resources/optimizationRunId.txt","w") as file:
        file.write(f"{newOptimizationRunId}")

def changeNumberOfContainersRunning(iContainer): 
    if iContainer > 0:
        subprocess.call(f"docker service scale app_app={iContainer}", shell=True)

for iContainer in numeroContainers:
    changeNumberOfContainersRunning(iContainer)
    for iRodada in range(0,rodadasPorNumeroDeContainers):        
        createOptimizationRunResponse = createOptimizationRun()
        optimizationRunFinished = False
        currentOptimizationRunId = createOptimizationRunResponse.id
        changeOptimizationRunIdFile(createOptimizationRunResponse.id)
        while not optimizationRunFinished:
            getOptimizationRunResponse = optimizationRunApi.get_optimization_run(id=currentOptimizationRunId)
            optimizationRunFinished = getOptimizationRunResponse.status == "FINISHED"
            time.sleep(1)
        if iContainer not in collectedData:
            collectedData[iContainer] = dict()
        if "timeToFinishInSeconds" not in collectedData[iContainer]:
            collectedData[iContainer]["timeToFinishInSeconds"] = list()
        collectedData[iContainer]["timeToFinishInSeconds"].append(getOptimizationRunResponse.time_to_finish_in_seconds)
        collectedData["bestFitness"].append(getOptimizationRunResponse.best_so_far_chromosome.fitness)
        collectedData["bestChromosomeElements"].append(getOptimizationRunResponse.best_so_far_chromosome.elements)

print(collectedData)
with open("collectedData.json", 'w') as file:
    json.dump(collectedData, file)

subprocess.call("docker service rm app_app", shell=True)