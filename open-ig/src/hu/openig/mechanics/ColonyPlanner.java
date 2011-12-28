/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.TaxLevel;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Colony planner. Constructs civilian buildings,
 * ensures the colony is in good health (similarly to autobuild).
 * Builds social buildings to keep the morale.
 * Adjusts taxes according to morale.
 * Builds factories and trading buildings.
 * May demolish damanged buildings.
 * May block other planners with empty action to gain money.
 * @author akarnokd, 2011.12.28.
 */
public class ColonyPlanner extends Planner {
	/**
	 * Constructor.
	 * @param world the world
	 * @param controls the controls
	 */
	public ColonyPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}

	@Override
	protected void plan() {
		if (checkColonyHub()) {
			return;
		}
		if (checkTax()) {
			return;
		}
		if (checkBuildingHealth()) {
			return;
		}
		if (checkPower()) {
			return;
		}
		if (checkWorker()) {
			return;
		}
		if (checkMorale()) {
			return;
		}
		if (checkLivingSpace()) {
			return;
		}
		if (checkFood()) {
			return;
		}
		if (checkHospital()) {
			return;
		}
		if (checkPolice()) {
			return;
		}
		if (checkFireBrigade()) {
			return;
		}
	}
	/**
	 * Ensure that buildings are repaired. 
	 * @return true if action taken
	 */
	boolean checkBuildingHealth() {
		// if low on money
		if (world.money < 10000) {
			// stop repairing
			boolean anyConstruction = false;
			for (final AIPlanet planet : world.ownPlanets) {
				if (planet.statistics.constructing) {
					anyConstruction = true;
					for (final AIBuilding b : planet.buildings) {
						if (b.repairing) {
							add(new Action0() {
								@Override
								public void invoke() {
									controls.actionRepairBuilding(planet.planet, b.building, false);
								}
							});
							return true;
						}
					}
				}
			}
			if (anyConstruction) {
				return true;
			}
		}
		// find and start repairing the cheapest damaged building per planet
		for (final AIPlanet planet : world.ownPlanets) {
			for (final AIBuilding b : planet.buildings) {
				if (b.repairing) {
					return true; // don't let other things continue
				}
			}
			AIBuilding toRepair = null;
			for (final AIBuilding b : planet.buildings) {
				if (b.isDamaged() && !b.repairing) {
					if (toRepair == null || toRepair.type.cost > b.type.cost) {
						toRepair = b;
					}
				}
			}
			if (toRepair != null) {
				final AIBuilding b = toRepair;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionRepairBuilding(planet.planet, b.building, true);
					}
				});
				return true;
			}
		}
		return false;
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkFireBrigade() {
		BuildingSelector food = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("repair");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("repair") && limit(planet, value, 1);
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(25000, o1.population, 
						25000, o2.population);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				// check if there is no or at least one upgradable fire department
				if (value.population < 25000) {
					return false;
				}
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, planetOrder, food, costOrder, true);
	}
	/**
	 * Issue a change taxation command.
	 * @param planet the target planet
	 * @param tax the new tax level
	 */
	void setTaxLevelAction(final AIPlanet planet, final TaxLevel tax) {
		add(new Action0() {
			@Override
			public void invoke() {
				controls.actionSetTaxation(planet.planet, tax);
			}
		});
	}
	/**
	 * Check for low or high morale and adjust taxation to take advantage of it.
	 * @return true if action taken
	 */
	boolean checkTax() {
		// try changing the tax
		for (AIPlanet planet : world.ownPlanets) {
			int moraleNow = planet.morale;
			TaxLevel tax = planet.tax;
			
			if (moraleNow < 25 || planet.population < 4500) {
				if (tax != TaxLevel.NONE) {
					setTaxLevelAction(planet, TaxLevel.NONE);
					return true;
				}
			} else
			if (moraleNow < 38 || planet.population < 5000) {
				if (tax != TaxLevel.VERY_LOW) {
					setTaxLevelAction(planet, TaxLevel.VERY_LOW);
					return true;
				}
			} else
			if (moraleNow < 55 || planet.population < 5500) {
				if (tax != TaxLevel.LOW) {
					setTaxLevelAction(planet, TaxLevel.LOW);
					return true;
				}
			} else
			if (moraleNow < 60) {
				if (tax != TaxLevel.MODERATE) {
					setTaxLevelAction(planet, TaxLevel.MODERATE);
					return true;
				}
			} else
			if (moraleNow < 65) {
				if (tax != TaxLevel.ABOVE_MODERATE) {
					setTaxLevelAction(planet, TaxLevel.ABOVE_MODERATE);
					return true;
				}
			} else
			if (moraleNow < 70 && planet.population > 10000) {
				if (tax != TaxLevel.HIGH) {
					setTaxLevelAction(planet, TaxLevel.HIGH);
					return true;
				}
			} else
			if (moraleNow < 78 && planet.population > 15000) {
				if (tax != TaxLevel.VERY_HIGH) {
					setTaxLevelAction(planet, TaxLevel.VERY_HIGH);
					return true;
				}
			} else
			if (moraleNow < 85 && planet.population > 20000) {
				if (tax != TaxLevel.OPPRESSIVE) {
					setTaxLevelAction(planet, TaxLevel.OPPRESSIVE);
					return true;
				}
			} else
			if (moraleNow < 95 && planet.population > 25000) {
				if (tax != TaxLevel.EXPLOITER) {
					setTaxLevelAction(planet, TaxLevel.EXPLOITER);
					return true;
				}
			} else {
				if (tax != TaxLevel.SLAVERY) {
					setTaxLevelAction(planet, TaxLevel.SLAVERY);
					return true;
				}
			}
			
		}
		return false;
	}
	/** @return check the morale level and build social buildings in necessary. */
	boolean checkMorale() {
		// if morale is still low, build a morale boosting building
		List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
		Collections.sort(planets, new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				int c = o1.morale - o2.morale;
				if (c == 0) {
					c = o1.population - o2.population;
				}
				return c;
			}
		});
		for (AIPlanet planet : planets) {
			int moraleNow = planet.morale;
			int moraleLast = planet.lastMorale;
			
			if (moraleNow < 21 && moraleLast < 27 && !planet.statistics.constructing) {
				if (boostMoraleWithBuilding(planet)) {
					return true;
				}
			}
			
		}
		return false;
	}
	/**
	 * Try building/upgrading a morale boosting building.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean boostMoraleWithBuilding(final AIPlanet planet) {
		BuildingSelector morale = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("morale");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("morale");
			}
		};
		return manageBuildings(planet, morale, costOrder, true);
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkFood() {
		BuildingSelector food = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("food");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("food");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(o1.statistics.foodAvailable, o1.population, o2.statistics.foodAvailable, o2.population);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.foodAvailable;
			}
		}, planetOrder, food, costOrder, true);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkPolice() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("police");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("police");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(o1.statistics.policeAvailable, o1.population, o2.statistics.policeAvailable, o2.population);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.policeAvailable * 1.1;
			}
		}, planetOrder, police, costOrder, true);
	}
	/**
	 * Check if there is shortage on hospitals.
	 * @return if action taken
	 */
	boolean checkHospital() {
		BuildingSelector hospital = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("hospital");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("hospital");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(o1.statistics.hospitalAvailable, o1.population, o2.statistics.hospitalAvailable, o2.population);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.hospitalAvailable * 1.1;
			}
		}, planetOrder, hospital, costOrder, true);
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkLivingSpace() {
		BuildingSelector livingSpace = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("house");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("house");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(o1.statistics.houseAvailable, o1.population, o2.statistics.houseAvailable, o2.population);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.houseAvailable;
			}
		}, planetOrder, livingSpace, costOrder, true);
	}
	/**
	 * Check if there is some worker demand issues,
	 * if so, try building morale and population growth boosting buildings.
	 * @return true if action taken
	 */
	boolean checkWorker() {
		BuildingSelector morale = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("morale") || value.hasResource("population-growth");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return (value.hasResource("morale") || value.hasResource("population-growth")) && limit(planet, value, 1);
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return o1.morale - o2.morale;
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population < value.statistics.workerDemand;
			}
		}, planetOrder, morale, costOrder, true);
	}
	/**
	 * Check if there are enough power on the planet,
	 * if not, try adding morale and growth increasing buildings.
	 * @return true if action taken
	 */
	boolean checkPower() {
		BuildingSelector energy = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding building) {
				return building.getEnergy() > 0;
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType buildingType) {
				return buildingType.hasResource("energy") && buildingType.getResource("energy") > 0;
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return worst(o1.statistics.energyAvailable, o1.statistics.energyDemand, 
						o2.statistics.energyAvailable, o2.statistics.energyDemand);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.statistics.energyAvailable < value.statistics.energyDemand;
			}
		}, planetOrder, energy, costOrder, true);	
	}
	/**
	 * Compares the numerical levels of the values and returns which one of it is worse.
	 * @param firstAvail the first availability
	 * @param firstDemand the first demand
	 * @param secondAvail the second availability
	 * @param secondDemand the second demand
	 * @return -1, 0 or 1
	 */
	int worst(int firstAvail, int firstDemand, int secondAvail, int secondDemand) {
		boolean firstOk = firstAvail >= firstDemand;
		boolean secondOk = secondAvail >= secondDemand;
		if (firstOk && !secondOk) {
			return 1;
		} else
		if (!firstOk && secondOk) {
			return -1;
		} else
		if (firstOk && secondOk) {
			return (secondAvail - secondDemand) - (firstAvail - firstDemand);
		}
		long v = (1L * secondDemand * firstAvail) - (1L * firstDemand * secondAvail);
		return v < 0 ? -1 : (v > 0 ? 1 : 0);
	}
	/**
	 * Check if a colony hub is available on planets,
	 * if not, try to build one.
	 * @return true if action taken
	 */
	boolean checkColonyHub() {
		// check for planets without colony hub first
		for (final AIPlanet planet : world.ownPlanets) {
			boolean found = false;
			for (Building b : planet.buildings) {
				if (b.type.kind.equals("MainBuilding")) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				final BuildingType bt = findBuildingKind("MainBuilding");
				if (world.money < bt.cost) {
					if (getMoreMoney(planet)) {
						return true;
					} else {
						// if no money could be gained, simply wait for the next day
						addEmpty();
						return true;
					}
				}
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionPlaceBuilding(planet.planet, bt);
					}
				});
				return true;
			}
		}
		// check planets with damaged colony hub
		for (final AIPlanet planet : world.ownPlanets) {
			for (final AIBuilding b : planet.buildings) {
				if (b.type.kind.equals("MainBuilding")) {
					if (b.isDamaged() && !b.repairing) {
						controls.actionRepairBuilding(planet.planet, b.building, true);
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Try to get more money by selling buildings.
	 * @param current the current planet where to look for buildings first
	 * @return true if action taken
	 */
	boolean getMoreMoney(final AIPlanet current) {
		List<Func1<Building, Boolean>> functions = JavaUtils.newArrayList();
		// severly damaged
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return value.isSeverlyDamaged();
			}
		});
		// damaged
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return value.isDamaged();
			}
		});
		// any
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return true;
			}
		});
		for (Func1<Building, Boolean> f : functions) {
			if (findDamaged(current, f)) {
				return true;
			}
			for (AIPlanet planet : world.ownPlanets) {
				if (planet != current) {
					if (findDamaged(planet, f)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Find the cheapest damaged building and issue a demolish order.
	 * @param current the current planet
	 * @param check the check function.
	 * @return true if action taken
	 */
	boolean findDamaged(final AIPlanet current, final Func1<Building, Boolean> check) {
		AIBuilding cheapest = null;
		for (AIBuilding b : current.buildings) {
			if (check.invoke(b)) {
				if (cheapest == null || cheapest.type.cost < b.type.cost) {
					cheapest = b;
				}
			}
		}
		if (cheapest != null) {
			final AIBuilding fcheapest = cheapest;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionDemolishBuilding(current.planet, fcheapest.building);
				}
			});
			return true;
		}
		return false;
	}
}