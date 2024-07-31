import requests

base_url = "http://localhost:8080"

optimization_run_id = 1



def get_not_evaluated_chromosome(optimization_run_id: int)->object:
    get_chormossome_path = f"/optimizationRun/{optimization_run_id}/chromosome/notEvaluated"
    response = requests.request("GET", base_url + get_chormossome_path)

def post_evaluation_result(chromosome_id: int, evaluation_id: str, fitness: float)-> object:
    post_response_path = f"/chromosome/{chromosome_id}/evaluationResult"
    payload = {
        "fitness": fitness,
        "evaluationId": evaluation_id
    }
    headers = {"Content-Type": "application/json"}
    response = requests.request("POST", base_url + post_response_path, json=payload, headers=headers)
    return response.json

