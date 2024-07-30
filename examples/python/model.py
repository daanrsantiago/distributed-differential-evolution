import numpy as np

def model(t: float, y: list[float], ts: list[float], control: list[int]) -> np.ndarray[float]:

    if(t < ts[0]):
        u1 = control[0]
    elif(t >= ts[0] and t < ts[1]):
        u1 = control[1]
    else:
        u1 = control[2]

    if(t < ts[2]):
        u2 = control[3]
    elif(t >= ts[2] and t < ts[3]):
        u2 = control[4]
    else:
        u2 = control[5]

    m1 = 1.0
    m2 = 1.0

    c1 = 1.0
    c2 = 2.0
    c3 = 1.0

    k1 = 3.0
    k2 = 3.0
    k3 = 3.0

    x = np.array([[y[0]],[y[1]],[y[2]],[y[3]]])
    u = np.array([[u1],[u2]])

    B = np.array(
        [[0      , 0     ], 
        [1/m1   , 0     ], 
        [0      , 0     ], 
        [0      , 1/m2  ]]
    )

    A = np.array(
        [[0, 1, 0, 0],
        1/m1 * np.array([-(k1+k2), -(c1+c2), k2, c2]),
        [0, 0, 0, 1],
        1/m2 * np.array([k2, c2, -(k2+k3), -(c2+c3)])]
    )

    return np.append(np.dot(A,x) + np.dot(B,u), u1 + u2 )