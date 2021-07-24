def names = []
def iterations = []
def minPacing = []
def loadGeneratorsNumber = []

def step_start = []
def step_finish = []



steps = 7
k = 0
String profile = vars.get("profile");
String file = new File(profile).eachLine() {line, lineNum ->

       if(lineNum == 1) {
            return
        }


	log.info("LINE:" + line.toString());

	var0 = ""
	i = 0
	
	while(line[i] != ',') {
		var0 += line[i]
		++i
	}
	++i
	names[k] = var0
	var0 = ""
	while(line[i] != ',') {
		var0 += line[i]
		++i
	}
	++i
	iterations[k] = var0.toInteger()
	var0 = ""
	while(line[i] != ',') {
		var0 += line[i]
		++i
	}
	++i
	minPacing[k] = Double.parseDouble(var0)
	var0 = ""
	while(i != line.size()) {
		var0 += line[i]
		++i
	}
	loadGeneratorsNumber[k] = var0.toInteger()
	log.info("LGNUMBERS:" + loadGeneratorsNumber[k].toString());
	++k
}
 // обнуляем проперти
props.put("baseline_rampup","0");
props.put("baseline_duration","0");
props.put("step_rampup","0");
props.put("step_duration","0");
props.put("lg_slow","0");
props.put("test_lenght", "0");
props.put("step_start0", "0");
props.put("step_finish0", "0");
props.put ("step1_duration_in_min", "0");
props.put ("step2-7_duration_in_min", "0");
props.put("currentStepNumber","0")

for (i = 0; i < names.size(); i++){
	var1 = names[i] + "_start"
	props.put(var1, "0")
	
	var2 = names[i] + "_add_thread"
	props.put(var2, "0")
	
	var3 = names[i] + "_throughtput"
	props.put(var3, "0")

	var4 = names[i] + "_load_generators"
	props.put(var4, "0")

	props.put(names[i] + "_baseline", "0")
}
	
for (i = 1; i <= steps; ++i) {
	z = steps - i + 1;
	var4 = z + "_length"	
	props.put(var4, "0")

	var5 = i + "_init_delay"	
	props.put(var5, "0")
	
	if(i!=steps){
	props.put("step_start"+i, "0");
	props.put("step_finish"+i, "0");	
	}
}
	

//расчет ступеней
baseline_rampup = vars.get("baseline_rampup") as int
baseline_percent = Double.parseDouble(vars.get("baseline_percent"))/100
baseline_duration = vars.get("baseline_duration") as int

if(vars.get("step_rampup").equals("")){
	vars.put("step_rampup", "0");	
}
if(vars.get("step_percent").equals("")){	
	vars.put("step_percent", "0")
}
if(vars.get("step_duration").equals("")){
	vars.put("step_duration", "0")
}

step_rampup = vars.get("step_rampup") as int
step_percent = Double.parseDouble(vars.get("step_percent"))/100
step_duration = vars.get("step_duration") as int
lg_slow= vars.get("lg_slow") as String

props.put("baseline_rampup",String.valueOf(baseline_rampup));
props.put("baseline_duration",String.valueOf(baseline_duration));
props.put("step_rampup",String.valueOf(step_rampup));
props.put("step_duration",String.valueOf(step_duration));
props.put("lg_slow",String.valueOf(lg_slow));


for (i = 0; i < names.size(); ++i){

	if (step_percent == 0)
		minStepLoad = iterations[i] * baseline_percent / loadGeneratorsNumber[i]
	else
		minStepLoad = iterations[i] * step_percent / loadGeneratorsNumber[i]
	oneUserLoadGeneration = 3600 / minPacing[i]
	minStepVUNumber = (Math.ceil(minStepLoad / oneUserLoadGeneration)).toInteger()
	oneVUOperationsNumber = minStepLoad / minStepVUNumber
	
	oneVUOperationsPerMinNumber = oneVUOperationsNumber / 60

	if (step_percent == 0)
		baseLineVUNumber = minStepVUNumber.toInteger()
	else
		baseLineVUNumber = (minStepVUNumber * (baseline_percent / step_percent)).toInteger()

	log.info("===========================\n\n\n");
	log.info("threadGroup [" + i + "] - " + names[i])								//имя тредгруппы
	log.info("iterations [" + i + "] - " + iterations[i])							//операций в час
	log.info("loadGeneratorsNumber [" + i + "] - " + loadGeneratorsNumber[i])		//на какое количество LG делится нагрузка
	log.info("step_percent [" + i + "] - " + step_percent)						//процент профиля на ступеньках теста
	log.info("minStepLoad [" + i + "] - " + minStepLoad)							//сколько операций/час добавляется на ступеньках
	log.info("oneUserLoadGeneration [" + i + "] - " + oneUserLoadGeneration)		//один поток выдаёт операций/час
	log.info("minStepVUNumber [" + i + "] - " + minStepVUNumber)					//количество потоков добавляется на ступеньке
	log.info("oneVUOperationsPerMinNumber [" + i + "] - " + oneVUOperationsPerMinNumber)	//один поток выдаёт операций/минуту
	log.info("baseLineVUNumber [" + i + "] - " + baseLineVUNumber)				//количество потоков на первой ступеньке теста
	log.info("\n\n\n")
	
		
	var1 = names[i] + "_start"
	props.put(var1, String.valueOf(baseLineVUNumber))
	
	var2 = names[i] + "_add_thread"
	props.put(var2, String.valueOf(minStepVUNumber))
	
	var3 = names[i] + "_throughtput"
	props.put(var3, String.valueOf(oneVUOperationsPerMinNumber))

	var4 = names[i] + "_load_generators"
	props.put(var4, String.valueOf(loadGeneratorsNumber[i]))

	props.put(names[i] + "_baseline", String.valueOf(iterations[i]))
}

all_length = baseline_duration + (step_rampup + step_duration) * (steps - 1)
props.put("test_lenght", (all_length * 1000).toString());
for (i = 1; i <= steps; ++i) {
	z = steps - i + 1;
	var4 = z + "_length"
	if (i == 7)
		l = all_length
	else
		l = all_length - (step_rampup + step_duration) * (steps - i - 1) - step_rampup - baseline_duration
	props.put(var4, String.valueOf(l))

	var5 = i + "_init_delay"
	if (i == 1)
		ind = 0
	else if (i == 2)
		ind = baseline_rampup + baseline_duration
	else {
		n = i-1
		m = n + "_init_delay"
		k = props.get(m) as int
		ind = k + step_duration + step_rampup
	}
	props.put(var5, String.valueOf(ind))
}
//получение границ ступенек
startTestTime = props.get("TESTSTART.MS") as Long
step_start[0] = baseline_rampup;
step_finish[0] = baseline_rampup + baseline_duration;

props.put("step_start0", (new Date(step_start[0] * 1000 + startTestTime).format("yyyy-MM-dd HH:mm:ss")));
props.put("step_finish0", (new Date(step_finish[0] * 1000 + startTestTime).format("yyyy-MM-dd HH:mm:ss")));

//получаем длительность ступеней
props.put ("step1_duration_in_min", new Date(baseline_duration * 1000 - (3 * 3600000)).format("HH:mm:ss") );
props.put ("step2-7_duration_in_min", new Date(step_duration * 1000 - (3 * 3600000)).format("HH:mm:ss") );

log.info("Начало ступени 0 "  + props.get("step_start0"));
log.info("Конец ступени 0 " + props.get("step_finish0"));

for (i = 1; i < 7; ++i)
{
	step_start[i] = step_finish[i-1] + step_rampup ;	
	step_finish[i] = step_start[i] + step_duration; 
	props.put("step_start"+i, (new Date(step_start[i] * 1000 + startTestTime).format("yyyy-MM-dd HH:mm:ss")));
	props.put("step_finish"+i, (new Date(step_finish[i] * 1000 + startTestTime ).format("yyyy-MM-dd HH:mm:ss")));
	log.info("Начало ступени " + i + " - " + props.get("step_start"+i));
	log.info("Конец ступени " + i + " - " + props.get("step_finish"+i));	
}

log.info("===========================\n\n\n");

currentTime = System.currentTimeMillis()
a = (startTestTime - currentTime) / 1000
for (int i = 1; i < 8; ++i){
	start = props.get(i+"_init_delay") as int
	if (i == 7){
		props.put("currentStepNumber",String.valueOf(i))
		break
	}
	k = i + 1
	finish = props.get(k+"_init_delay") as int
	if (a >= start && a <= finish){
		props.put("currentStepNumber",String.valueOf(i))
		break
	}
}


