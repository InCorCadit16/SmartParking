import easyocr
import cv2
import imutils


def parse_number(plateNumber):
    import re

    if plateNumber == "":
        plateNumber = 'NOT REC'

    incorrect_chars = ["TOYOTA", "KIA", "BMW", "_", ":", ",", "MD", "#", "~", ".", "@", "(", ")", "[", "", '-',
                       "callmed", "www", "CALLMED", "md"]
    for z in incorrect_chars:
        plateNumber = plateNumber.replace(z, "")
    regex = "[a-z]"
    plateNumber = re.sub(regex, "", plateNumber)

    if len(plateNumber.replace("  ", "").replace(" ", "")) == 6:
        f_p = "".join(plateNumber)[:3]
        s_p = "".join(plateNumber)[3:]

        plateNumber = str(f_p) + str(s_p)
        plateNumber = plateNumber.replace("  ", " ")
    if plateNumber[0] == " ":
        plateNumber = plateNumber[1:]

    if (len(plateNumber.split(" ")) == 2 and len(plateNumber.split(" ")[0]) == 4):
        f_p = plateNumber.split(" ")[0][:3]
        s_p = plateNumber.split(" ")[1]

        plateNumber = f_p + " " + s_p

    if len("".join(plateNumber.split(" "))) == 6:
        f_p = "".join(plateNumber.split(" "))[:3]
        s_p = "".join(plateNumber.split(" "))[3:]

        plateNumber = f_p + " " + s_p

    if (len("".join(plateNumber.split(" "))) == 7 and "".join(plateNumber.split(" "))[4].isdigit() == True):
        f_p = "".join(plateNumber.split(" "))[:3]
        s_p = "".join(plateNumber.split(" "))[4:]

        plateNumber = f_p + " " + s_p

    return plateNumber


def plate_number_recognition(image):
    reader = easyocr.Reader(['en'])
    image = imutils.resize(image, width=300)

    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray_image = cv2.bilateralFilter(gray_image, 11, 17, 17)

    edged = cv2.Canny(gray_image, 30, 200)

    cnts,new = cv2.findContours(edged.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    image1 = image.copy()
    cv2.drawContours(image1, cnts, -1, (0, 255, 0), 3)

    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)[:110]
    screenCnt = None
    image2 = image.copy()
    cv2.drawContours(image2, cnts, -1, (0, 255, 0), 3)

    i = 7
    for c in cnts:
        perimeter = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.018 * perimeter, True)
        if len(approx) == 4:
            screenCnt = approx
            i += 1
            break

    #cv2.drawContours(image, [screenCnt], -1, (0, 255, 0), 3)

    result = reader.readtext(image)
    result = parse_number(' '.join([x[1] for x in result]))

    return result
